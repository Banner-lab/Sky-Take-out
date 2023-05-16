package com.sky.task;

import com.sky.constant.MessageConstant;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.asm.Advice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @ClassName OrderTask
 * @Description 订单处理定时任务
 * @Author XMING
 * @Date 2023/5/16 10:23
 * @Version 1.0
 */
@Slf4j
@Component
public class OrderTask {
    @Autowired
    OrderMapper orderMapper;
    /**
     * 每分钟检查订单是否超时
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void orderTask(){
        log.info("每隔一分钟检查订单超时");
        // 查询超时订单
        List<Orders> orders = orderMapper.queryTimeOut(LocalDateTime.now().plusMinutes(-15),Orders.PENDING_PAYMENT);

        // 没有超时订单
        if(orders == null || orders.size() == 0){
            return ;
        }
        // 将超时订单状态改为取消
        orders.forEach(order -> {
            order.setStatus(Orders.CANCELLED);
            order.setCancelReason("订单超时，自动取消");
            order.setCancelTime(LocalDateTime.now());
            orderMapper.update(order);
        });
    }

    /**
     * 每天1点检查处于派送中状态的订单，将其修改为完成状态
     */
    @Scheduled(cron = "5 0/1 * * * ?")
    public void deliveryCheckTask() {
        log.info("每天凌晨1点检查订单状态，将位于派送中的订单修改为已完成状态");
        // 查询派送中订单

        List<Orders> orders = orderMapper.queryTimeOut(LocalDateTime.now().plusMinutes(-60),Orders.DELIVERY_IN_PROGRESS);
        if (orders == null || orders.size() == 0) {
            return ;
        }
        orders.forEach(order -> {
            order.setStatus(Orders.COMPLETED);
            orderMapper.update(order);
        });
    }
}
