# 项目一Day10项目日志

### 1.SpringTask有什么作用？使用步骤有哪些？

```
任务调用框架：可以按照约定的时间自动执行某段代码
1.导入spring-context坐标
2.开启任务调用：在启动类上加@EnableScheduling
3.定义类，在类中定义方法，方法上加上@Scheduled注解，指定cron表达式(按照表达式的指定时间去指定对应的方法)

```



### 2.Cron表达式有哪几个值组成?实现以下几个需求的Cron

```
表达式组成：
格式: 秒 分 时 日 月 周 年
注意事项; 日和周不要同时
需求：
1. 每天早上八点中执行 
	0 0 8 * * ?
2. 每10分钟执行一次
    0 0/10 * * * ?	
3. 每个月十号下午6点执行一次
	0 0 18 10 * ? 
```



### 3.支付超时订单应该如何处理

```java
思路：每分钟检查一次并处理超时订单，超时条件为当前时间与下单时间时间差超过15分钟，处理超时订单即修改订单状态为已取消
在OrderTask类中定义方法checkTimeOut()作为定时任务，指定cron表达式为"0 * * * * ?",调用orderService完成检查订单支付是否超时的任务，在orderService的checkPayTimeOut()中，调用orderMapper查询orders表，检查是否有符合超时条件的订单，如果有更新订单状态为已取消
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
```



### 4.WebSocket和Http有什么区别？

```
WebSocket基于tcp协议的新的网络通信协议，客户端浏览器和服务器双向通信
区别:
	1. http只能由客户端浏览器发送请求至服务器，websocket可以实现浏览器和服务器双向通信
	2. websocket持久连接，http是短连接
	3. http是无状态单向连接，websocket有状态的双向连接
```



### 5.用户催单功能实现步骤是什么？

```java
因为催单是用户点击历史订单中的催单按钮，由服务器发送消息给商家浏览器，提醒接单
步骤：
	1. 前端：用户点击催单按钮发送催单请求 /user/order/reminder/{id}
	2. 控制层: 定义方法，接受用户传来的订单id，调用orderService完成催单
	3. 服务层: 查询待接单的订单，注入websocketServer，调用群发消息的方法，将约定好的消息字符串发送到商家浏览器
	@Override
    public void reminder(Long id) {
        Orders order = orderMapper.queryById(id);
        if(order == null ||!order.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        Map map = new HashMap();
        map.put("type",2);
        map.put("orderId",order.getId());
        map.put("content","订单号:"+order.getNumber());

        String jsonString = JSON.toJSONString(map);

        webSocketServer.sendToAllClient(jsonString);
    }
```

