package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.service.OrderService;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @ClassName OrderServiceImpl
 * @Description TODO
 * @Author XMING
 * @Date 2023/5/13 15:21
 * @Version 1.0
 */
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Autowired
    UserMapper userMapper;
    // 百度地图api ak
    @Value("${sky.baidu.ak}")
    private String ak;

    // 店家地址
    @Value("${sky.shop.address}")
    private String shopAddress;

    // 百度地图api 地理编码api接口访问地址
    String url = "https://api.map.baidu.com/geocoding/v3/";

    // 百度地图api 路线规划接口api
    String urlDistance  = "https://api.map.baidu.com/directionlite/v1/riding";


    /**
     * 下订单
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());

        // 地址为空 抛出异常
        if(addressBook == null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        String orderAddress = addressBook.getProvinceName()+addressBook.getCityName()+addressBook.getDistrictName()+addressBook.getDetail();

        // 计算配送距离
        if(distanceBetween(shopAddress,orderAddress)){
            throw  new AddressBookBusinessException("配送距离超过5km，无法下单");
        }


        // 查询购物车
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> cartList = shoppingCartMapper.list(shoppingCart);
        if(cartList == null || cartList.size() == 0){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //构造订单数据
        Orders order = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,order);
        order.setOrderTime(LocalDateTime.now());
        order.setPhone(addressBook.getPhone());
        order.setAddress(addressBook.getDetail());
        order.setConsignee(addressBook.getConsignee());
        order.setUserId(BaseContext.getCurrentId());
        order.setStatus(Orders.PENDING_PAYMENT);
        order.setPayStatus(Orders.UN_PAID);
        // 生成订单号
        String number = UUID.randomUUID().toString().replace("-","");
        order.setNumber(number);
        // 往order表中插入一条记录
        orderMapper.insert(order);

        // 生成orderDetail数据
        List<OrderDetail> list = new ArrayList<>();
        for (ShoppingCart cart : cartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart,orderDetail);
            orderDetail.setOrderId(order.getId());
            list.add(orderDetail);
        }

        orderDetailMapper.insertBatch(list);

        // 清空购物车数据
        shoppingCartMapper.deleteByUserId(BaseContext.getCurrentId());

        OrderSubmitVO orderSubmitVO = new OrderSubmitVO();
        orderSubmitVO.setOrderTime(order.getOrderTime());
        orderSubmitVO.setOrderAmount(order.getAmount());
        orderSubmitVO.setOrderNumber(order.getNumber());
        orderSubmitVO.setId(order.getId());

        return orderSubmitVO;
    }

    /**
     * 计算店家和配送地址距离
     * @param shopAddress
     * @param orderAddress
     * @return
     */
    private Boolean distanceBetween(String shopAddress,String orderAddress) {

        // 设置地理编码接口参数
        Map<String,String> params = new HashMap<>();
        params.put("ak",ak);
        params.put("output","json");
        params.put("address",shopAddress);

        // 获取店家地理编码信息,解析相应结果，返回的是json
        String showLocation = HttpClientUtil.doGet(url, params);
        JSONObject location = (JSONObject) JSONObject.parseObject(showLocation);

        if(!location.getString("status").equals("0")){
            throw new OrderBusinessException("店铺地址解析失败");
        }

        // 获取店家位置经纬度
        BigDecimal lat = (BigDecimal) location.getJSONObject("result").getJSONObject("location").get("lat");
        BigDecimal lng = (BigDecimal) location.getJSONObject("result").getJSONObject("location").get("lng");
        log.info("店家位置: 纬度{} ,经度{}",lat,lng);

        // 查询订单地址经纬度
        params.put("address", orderAddress);
        String showLocation1 = HttpClientUtil.doGet(url, params);
        JSONObject location1 = (JSONObject) JSONObject.parseObject(showLocation1);
        if(!location1.getString("status").equals("0")){
            throw new OrderBusinessException("订单地址解析失败");
        }


        // 获取店家位置经纬度
        BigDecimal lat1 = (BigDecimal) location1.getJSONObject("result").getJSONObject("location").get("lat");
        BigDecimal lng1 = (BigDecimal) location1.getJSONObject("result").getJSONObject("location").get("lng");
        log.info("订单位置: 纬度{} ,经度{}",lat1,lng1);

        // 设置百度地图api 路线规划api参数
        DecimalFormat df = new DecimalFormat("0.000000");
        String latShop = df.format(lat);
        String lngShop = df.format(lng);

        String latOrder = df.format(lat1);
        String lngOrder = df.format(lng1);


        // 计算用户地址与店家地址距离
        // 起点 经纬度
        String origin = latShop+","+lngShop;
        // 目的地 经纬度
        String destination = latOrder+","+lngOrder;

        Integer distance = distance(origin,destination);
        return distance > 5000;
    }

    private Integer distance(String origin, String destination) {
        // 设置百度地图api 路线规划参数
        Map<String,String> param = new HashMap<String,String>();
        param.put("ak",ak);
        param.put("origin",origin);
        param.put("destination",destination);

        String result = HttpClientUtil.doGet(urlDistance, param);
        JSONObject res = (JSONObject) JSONObject.parseObject(result).getJSONObject("result");

        if(!res.getString("status").equals("0")){
            throw new OrderBusinessException("配送路线规划失败");
        }

        JSONArray jsonArray = (JSONArray) res.get("routes");
        Integer distance = (Integer) ((JSONObject)jsonArray.get(0)).get("distance");
        log.info("距离：{}",distance);
        return distance;
    }


    /**
     * 查询历史订单
     * @param
     * @return
     */
    @Override
    public PageResult historyOrders(Integer page,Integer pageSize,Integer status) {
        // 条件查询用户订单
        PageHelper.startPage(page,pageSize);
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setStatus(status);
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());

        Page<Orders> pages = orderMapper.pageQuery(ordersPageQueryDTO);
        List<OrderVO> orderVOList = new ArrayList<>();
        if(pages != null && pages.getTotal() > 0){

            for (Orders orders : pages.getResult()) {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders,orderVO);
                List<OrderDetail> details = orderDetailMapper.queryByOrderId(orders.getId());
                orderVO.setOrderDetailList(details);
                orderVOList.add(orderVO);
            }
        }
        return new PageResult(orderVOList,pages.getTotal());
    }

    /**
     * 查看订单详情
     * @param id
     * @return
     */
    @Override
    public OrderVO historyOrder(Long id) {
        Orders order = orderMapper.queryById(id);

        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order,orderVO);
        List<OrderDetail> details = orderDetailMapper.queryByOrderId(order.getId());
        orderVO.setOrderDetailList(details);
        return orderVO;
    }

    /**
     * 取消订单
     * @param id
     */
    @Override
    public void cancel(Long id) {
        // 先查询要取消的订单
        Orders order = orderMapper.queryById(id);
        if(order == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        // 需要电话和商家沟通，无法直接取消
        if (order.getStatus()>2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        // 订单处于待接单状态下,需要进行退款,模拟退款
        if(order.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            log.info("模拟用户退款");
            order.setPayStatus(Orders.REFUND);
        }
        order.setStatus(Orders.CANCELLED);
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason("用户取消");
        orderMapper.update(order);
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @Override
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.queryById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    @Override
    public void paySuccess(String outTradeNo) {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();

        // 根据订单号查询当前用户的订单
        Orders ordersDB = orderMapper.getByNumberAndUserId(outTradeNo, userId);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 再来一单子
     * @param id
     */
    @Override
    public void repetition(Long id) {
        // 查询订单细节
        List<OrderDetail> details = orderDetailMapper.queryByOrderId(id);
        List<ShoppingCart> carts = new ArrayList<>();
        Long userId = BaseContext.getCurrentId();
        details.forEach(item -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(item,shoppingCart);
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());
            carts.add(shoppingCart);
        });
        // 将购物车数据批量插入shopping_cart表
        shoppingCartMapper.insertBatch(carts);
    }

    /**
     * 条件查询订单
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        Page<Orders> orders = orderMapper.pageQuery(ordersPageQueryDTO);
        List<OrderVO> orderVOList = new ArrayList<>();
        orders.forEach(order->{
            List<OrderDetail> details = orderDetailMapper.queryByOrderId(order.getId());
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(order,orderVO);
            orderVO.setOrderDetailList(details);
            StringBuilder orderDishes = new StringBuilder();

            for(int i=0;i<details.size();i++){
                if(i == details.size() - 1){
                    orderDishes.append(details.get(i).getName() + "*"+details.get(i).getNumber());
                }
                else{
                    orderDishes.append(details.get(i).getName() + "*"+details.get(i).getNumber()).append(",");
                }
            }

            orderVO.setOrderDishes(orderDishes.toString());
            orderVOList.add(orderVO);
        });
        PageResult pr = new PageResult();
        pr.setTotal(orders.getTotal());
        pr.setRecords(orderVOList);
        return pr;
    }

    @Override
    public OrderVO detail(Long id) {
        Orders order = orderMapper.queryById(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order,orderVO);

        List<OrderDetail> details = orderDetailMapper.queryByOrderId(order.getId());
        orderVO.setOrderDetailList(details);
        return orderVO;
    }

    /**
     * 订单状态统计
     * @return
     */
    @Override
    public OrderStatisticsVO statistics() {
        return orderMapper.statistics();
    }

    /**
     * 接单
     * @param
     */
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        // 查询订单
        Long id = ordersConfirmDTO.getId();
        Orders order = orderMapper.queryById(id);
        if(order == null || !order.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        // 修改订单状态为接单
        order =new Orders();
        order.setStatus(Orders.CONFIRMED);
        order.setId(id);

        orderMapper.update(order);
    }

    /**
     * 派送订单
     * @param id
     */
    @Override
    public void delivery(Long id) {
        // 查询订单
        Orders order = orderMapper.queryById(id);
        if (order == null || !order.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        order = new Orders();
        // 修改订单状态为派送
        order.setStatus(Orders.DELIVERY_IN_PROGRESS);
        order.setId(id);
        orderMapper.update(order);
    }

    /**
     * 拒单
     * @param ordersRejectionDTO
     */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        //查询订单
        Orders order = orderMapper.queryById(ordersRejectionDTO.getId());
        if(order == null || !order.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        // 如果用户已经完成了支付需要为用户退款,需要修改支付状态为退款
        if(order.getPayStatus().equals(Orders.PAID)) {
            log.info("拒单id:{} 模拟退款", ordersRejectionDTO.getId());
        }

        order = new Orders();
        order.setId(ordersRejectionDTO.getId());
        order.setCancelReason(ordersRejectionDTO.getRejectionReason());
        order.setCancelTime(LocalDateTime.now());
        order.setStatus(Orders.CANCELLED);
        orderMapper.update(order);
    }

    /**
     * 取消订单
     * @param ordersCancelDTO
     */
    @Override
    public void cancelOrder(OrdersCancelDTO ordersCancelDTO) {
        // 查询订单
        Orders order = orderMapper.queryById(ordersCancelDTO.getId());
        if(order == null ||!order.getStatus().equals(Orders.CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        // 商家取消订单时，如果用户已经完成了支付，需要为用户退款
        if(order.getPayStatus().equals(Orders.PAID)){
            log.info("取消订单id:{} 模拟退款", order.getId());
        }

        order = new Orders();
        order.setId(ordersCancelDTO.getId());
        order.setCancelTime(LocalDateTime.now());
        order.setStatus(Orders.CANCELLED);
        order.setCancelReason(ordersCancelDTO.getCancelReason());
        orderMapper.update(order);
    }

    /**
     * 完成订单
     * @param id
     */
    @Override
    public void complete(Long id) {
        // 查询订单
        Orders order = orderMapper.queryById(id);
        // 只有订单处于派送中状态才可以完成订单
        if(order == null || !order.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        // 设置送达时间
        order.setDeliveryTime(LocalDateTime.now());
        // 修改订单状态为完成
        order.setStatus(Orders.COMPLETED);
        orderMapper.update(order);
    }
}
