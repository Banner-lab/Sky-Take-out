# 项目一Day11项目日志

### 1.Echarts是什么？

```
EChars是百度开源的图表库，
开发流程:
  - 分析前端图表需要什么样的数据。
  - 发送请求给后端，后端提供对应的数据。
  - 前端将响应数据提供给Echarts即可。
```



### 2.营业额统计功能如何实现？

```java
前端传递的请求参数：开始日期，结束日期 （统计数据的范围
需要响应给前端的数据：日期列表、营业额列表
实现分析：
	控制层 OrderController：接收开始和结束日期了，调用ReportService
	Service层: 
		定义通用方法dateList()处理日期
		private List<LocalDate> dateList(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        return dateList;
    }
	// 查询订单表，计算时间区间内每天的营业额
        List<Double> amountList = new ArrayList<>();
        dateList.forEach(d->{
            LocalDateTime start = d.atStartOfDay();
            LocalDateTime last = d.atTime(23,59,59);

            Double amount = 		orderMapper.queryByDate(start,last,Orders.COMPLETED);
            amount = amount == null ? 0.0 : amount;
            amountList.add(amount);
        });
```



### 3.用户统计功能如何实现？

```
需求：查询某一段时间内的用户总量和新增用户数量
控制层：做法和上文营业额统计相同，接受前端传递的开始时间和结束时间，调用service完成数据查询
业务层：
	  // 存放每天新增用户数量
        List<Integer> newUsers = new ArrayList<>();
        // 存放每天的总用户量
        List<Integer> totalUsers = new ArrayList<>();
        <select id="countUsers" resultType="java.lang.Integer">
        // 查询每天新增用户数
        select count(id) from user
            <where>
                <if test="begin != null">
                    create_time &gt; #{begin}
                </if>
                <if test="end != null">
                    and create_time &lt; #{end}
                </if>
            </where>
    	</select>
    	// 分别统计每日新增与截至当前日期用户数
    	 newUsers.add(cnt);
         totalUsers.add(cnt1);
        // 最后将结果封装为VO返回 
        return UserReportVO.builder().dateList(StringUtils.join(dateList,","))
                .newUserList(StringUtils.join(newUsers,","))
                .totalUserList(StringUtils.join(totalUsers,","))
                .build();
```



### 4.订单统计功能如何实现？

```
需求：把指定时间范围内的订单数据获取到，并在前端页面展示
  	实现分析：
    需要获取到哪些数据？
      日期列表：方法同营业额统计相同
      统计订单sql:
      	<select id="countOrders" resultType="java.lang.Integer">
        SELECT count(id) FROM orders
        <where>
            <if test="status != null">
                status = #{status}
            </if>
            <if test="begin != null">
                and order_time &gt; #{begin}
            </if>
            <if test="end != null">
                and order_time &lt; #{end}
            </if>

        </where>

    	</select>
      	有效订单总数: 已经完成的订单累加
       	订单总数：: 订单累加
       	for(LocalDate d : dateList){
            LocalDateTime start = d.atStartOfDay();
            LocalDateTime last = d.atTime(23,59,59);

            // 每日总订单数
            Integer countEveryDay = orderMapper.countOrders(start,last,null);
            Integer countCompletedEveryDay = orderMapper.countOrders(start,last,Orders.COMPLETED);

            countEveryDay = countEveryDay == null ? 0 : countEveryDay;
            countCompletedEveryDay = countCompletedEveryDay == null ? 0 : countCompletedEveryDay;
            total.add(countEveryDay);
            completed.add(countCompletedEveryDay);

            totalOrders += countEveryDay;
            completedOrders += countCompletedEveryDay;
        }
       	订单完成率列表：有效订单总数 / 订单总数

```



### 5.销量排名统计功能如何实现？

```
- 需求分析：

  - 需求：获取销量排名前十的商品和对应的销量值

  - 实现分析：

    - controller：接收开始和结束时间，调用service

    - service：查询数据库，获取获取销量排名前十的商品和对应的销量值，根据VO封装结果。

    - mapper：
     <select id="queryGoodsSalesTop10" resultType="com.sky.dto.GoodsSalesDTO">
        SELECT SUM(od.number) as number ,od.name as name FROM order_detail od INNER JOIN orders o
        ON o.id = od.order_id
        <where>
            o.status = 5
            <if test="begin != null">
                    and order_time &gt; #{begin}
            </if>
            <if test="end != null">
                    and order_time &lt; #{end}
            </if>
        </where>
        GROUP BY od.name ORDER BY SUM(od.number) DESC
        LIMIT 0 , 10
    </select>
```

