# 项目一Day06项目日志

HttpClient有什么作用？使用步骤是什么？

```
HttpClient作用:
	1.发送Http请求
	2.接受响应数据
利用Httpclient可以使用java代码发送http请求
HttpClient:Http客户端对象类型，使用该对象发送Http请求
HttpGet：Get方式请求类型
HttpPost: Post方式请求类型
使用步骤:
	- 创建HttpClient对象
	- 创建Http请求对象
	- 调用HttpClient的execute方法发送请求
	
```



微信小程序的授权码有什么用？

```
获取授权码后可调用微信接口服务，获取openid（微信用户唯一标识）后可自定义登录状态，并生成token
```

微信小程序的微信登录流程是怎样的？详细说明步骤

```
调用wx.login()获取临时登录凭证code，并回传到开发者服务器
调用auth.code2Session接口换取用户唯一标识openid,用户在微信平台开放账号下的唯一标识UnionID和会话密钥
1. 获取小程序用户的授权码code
2. 调用userService的微信登录业务，在service层具体的业务中，使用HttpClient发送http请求，调用微信接口服务，获取当前微信用户的openid
3.得到openid后，根据openid查询数据库user表，查询当前用户是否已存在，如果不存在，自动进行注册，返回用户信息给usercontroller
4.控制层获取用户信息后，为用户生成jwt令牌，jwt令牌，openid，以及id封装成userloginvo实体对象后返回给小程序前台
```



用户端菜品浏览有哪几个接口？

```
查询分类
根据分类id查询菜品
根据分类id查询套餐
根据套餐id查询包含的菜品
```

