# 项目一Day07日志

### 1.加缓存能解决什么问题？有什么注意事项？

```
当点餐时间大量请求进入后台，访问数据库获取菜品/套餐数据，此时数据库访问压力会陡增，会产生响应慢，用户体验差的问题，增加缓存就是为了实现对访问的优化。每次不再都访问数据库。
缓存的使用需要注意：缓存一致性问题，需要注意缓存与数据库数据不一致的问题
对于缓存一致性问题的解决方案：在执行增删改的时候清理缓存
```



### 2.缓存菜品的流程是什么？

```
根据分类id查询菜品时，先检查redis缓存中是否有菜品数据，如果有直接将结果返回给小程序
如果没有，再去数据库查询，将结果返回的同时，将数据缓存到redis中，方便下一次使用
具体流程：	
	在DishServiceimpl的listWithFlavor()方法中，先执行
    String key = "dish_"+categoryId;
        List<DishVO> list = (List<DishVO>)       redisTemplate.opsForValue().get(key);
     上述这段代码查询redis缓存，查询结果不为空直接将结果返回，反之需要查询数据库dish表，根据分类id查询菜品，将查询结果保存到redis缓存的同时返回结果
清理缓存：
	当对菜品数据执行了增删改操作时，需要清楚redis缓存的菜品数据，保存数据一致性
	在admin/DishController中定义清理缓存方法
	private void cleanCache(String pattern) {
    	Set keys = redisTemplate.keys(pattern);
    	redisTemplate.delete(keys);
	}	
	当执行add（新增菜品）、update（修改菜品）,delete(删除菜品)等增删改操作时，清楚缓存
     
```



### 3.SpringCache四个注解分别如何使用？

```
@EnableCaching 加在启动类上，开启注解缓存功能
@CachePut（value = "", key = ""） 将方法返回值加入redis缓存，一般放在新增方法上
	 - value属性：指定key所属分类名称
	 - key属性: 真正存放到reids中的key 
@CacheEvit(value = "" ,key = "") 删除指定缓存数据
@Cacheable 查询缓存，如果有，则直接返回数据，如果没有，执行方法，将返回值加入到缓存中
在本项目中，先在启动类上加上@EnableCaching注解开启缓存功能
在根据分类id查询套餐数据的方法上加@Cacheable(value = "setmealCache",key="#categoryId")
1. 在admin/SetmealContrller的add（新增套餐）的方法上加上@CacheEvict(value = "setmealCache",key = "#setmealDTO.categoryId") 注解，在新增套餐的同时删除原有的套餐数据
2. 在delete()方法上加上@CacheEvict(value = "setmealCache",allEntries = true),先删除缓存中所有的套餐信息
3. 起售禁售方法用法和delete方法相同不再赘述
```



### 4.添加购物车流程是什么？

```
1. 前端传递 dishId/setmealId 以及菜品口味
2. controller 接受参数，调用shoppingCartService的添加购物车方法
3. service：
	根据dishId/setmealId 查询购物车
	如果查询到的结构不为空说明购物车中已经有了当前商品，直接将商品数量加一，并执行update方法更新
	反之，先要去查询dish/setmeal表获取商品数据，将获得的商品信息封装为shoppingcart再调用mapper的insert方法，插入一条新的记录
```

