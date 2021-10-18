# resttemplate
从[restTemplate](https://github.com/developer-wenren/resttemplate.git) 复制而来。
[《一起学 Spring 之 RestTemplate 使用指南 》]() 一文的演示 Demo

# 参考资料
http://blog.didispace.com/spring-boot-learning-21-1-1

https://start.spring.io	"Spring Initializr"

https://www.baeldung.com/rest-template

https://www.baeldung.com/spring-rest-template-multipart-upload

https://howtodoinjava.com/spring-boot2/resttemplate-timeout-example/	"resttemplate-timeout-example"

https://howtodoinjava.com/spring-restful/resttemplate-httpclient-java-config/	"resttemplate-httpclient-java-config"

https://netty.io/	"Netty"

https://square.github.io/okhttp	"OkHttp"

https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/client/RestTemplate.html	"resttemplate javadoc"

 https://docs.spring.io/spring/docs/5.1.6.RELEASE/spring-framework-reference/integration.html#rest-client-access	"rest-client-access"

https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/client/RestOperations.html	"restoperation javadoc"
# TODO：
- 理解代码，并做笔记（注释)
- 按照需要，补充/更改一些代码
  - 我喜欢 get-product 风格的url，改

# 笔记
## REST APP
为了测试 RestTemplate 编写了简单的 REST 服务器。

REST只是建议的风格，并不强制必须全部遵守，可以根据实际情况对风格的约束做出取舍。
所以 /product/get-product 这种在URL中出现GET动词也是可以接受

先定义了实体类 Product，有ID，name，price 三个属性，并重载了 toString() 方法。

然后定义了 /product 下的几个子路径。
- get 方法的请求。以表单方式接收数据，以json格式返回数据
  - 返回的媒体类型都是 `Content-Type: application/json;charset=UTF-8`。
  - 接受的媒体类型都是，没有请求体，空这个，如果有参数，从url中传入
  - /product/get-product-1 不接受参数，返回固定的product对象(1, "product-a", 666.00)，json 格式。
  - /product/get-product-2 接收名为 id 的表单参数，返回对象 (id, "product-b", 666.00)，json 格式。
  - /product/get-product-3 接收客户端传入的 Product 对象（表单方式），然后返回对应的 toString() 结果，text/plain 类型。

- POST 方法的请求。
  - /product/post-product-1 接收客户端传入的 Product 对象（表单方式），然后返回对应的 toString 结果，text/plain 类型。
  - /product/post-product-2 接收客户端传入的 Product 对象（JSON方式），然后返回对应的 toString 结果，text/plain 类型。

- DELETE /product/delete/{id} 后台输出日志，返回一条文本消息. 
- PUT    /product/update      接收客户端传入的 Product 对象（表单方式），然后返回对应的 toString 结果，text/plain 类型。
- POST   /product/upload      接收传入的文件（文件名为file），服务器返回文本字符串。

## 测试 REST APP
使用 curl 做测试工具。
```bash
export host=localhost:8080
```
### get-product 系列

```bash
curl -m 1 $host/product/get-product-1
# {"id":1,"name":"ProductA","price":6666.0}

curl -m 1 $host/product/get-product-2
# {"id":null,"name":"ProductC","price":6666.0}

curl -m 1 $host/product/get-product-2?id=10
# {"id":10,"name":"ProductC","price":6666.0

curl -m 1 "$host/product/get-product-3?id=1&name=ProductC&price=9.99"
#Product{id='1', name='ProductC', price='9.99'}
```
使用 curl -v 参数还可以看到返回的媒体类型都是 `Content-Type: application/json;charset=UTF-8`。

### post-product 系列
```bash
# 表单方式传入，可以正常返回
curl -m 1 $host/product/post-product-1 -d id=1 -d name=ProductP -d price=99.9
#::Product{id='1', name='ProductP', price='99.9'}

# 缺少字段，也可以返回，只不过字段值为 null
curl -m 1 $host/product/post-product-1 -X POST
# :: Product{id='null', name='null', price='null'}

curl -m 1 "$host/product/post-product-2" -H "Content-Type: application/json; charset=UTF-8" -d '{ "id":1, "name": "product", "price": 99.8}'
# :: Product{id='1', name='product', price='99.8'}

# 默认是表单数据，后台会报错，提示不支持的媒体类型
curl -m 1 $host/product/post-product-2 -d id=1 -d name=ProductP -d price=99.9
# :: {"timestamp":"2021-10-18T16:16:26.325+0000","status":415,"error":"Unsupported Media Type",
# "message":"Content type 'application/x-www-form-urlencoded;charset=UTF-8' not supported",
# "path":"/product/post-product-2"}

# 只是设置媒体类型，却仍传入键值对，后台解析JSON失败
curl -m 1 $host/product/post-product-2 \
  -H "Content-Type: application/json; charset=UTF-8" \
  -d id=1 -d name=ProductP -d price=99.9
# :: 400 Bad Request 
# com.fasterxml.jackson.core.JsonParseException
```
其实，以 multipart/form-data 的形式提交POST请求，服务器一样可以正确解析
````bash
curl -m1 "$host/product/post-product-1" -F id=3  -v
Product{id='3', name='null', price='null'}
````
### delete/{id}
```bash
curl -m1 "$host/product/delete/1" -X DELETE
# :: 编号为1的产品删除成功

#########
# 不指定DELETE方法，会报 405 Method Not Allowed
curl -m1 "$host/product/delete/1"

# 缺少路径，会报 404 NOT Found
curl -m1 "$host/product/delete" -X DELETE
curl -m1 "$host/product/delete/" -X DELETE

# 路径参数不是数字，会报错 400 Bad Request，并给出异常信息
curl -m1 "$host/product/delete/ab" -X DELETE
# ::{"timestamp":"2021-10-18T16:37:54.035+0000",
#   "status":400,"error":"Bad Request",
#   "message":"Failed to convert value of type 'java.lang.String' to required type 'java.lang.Integer'; 
#       nested exception is java.lang.NumberFormatException: For input string: \"ab\"",
#    "path":"/product/delete/ab"}
```
### update
```bash
$ curl -m1 "$host/product/update" -X PUT -d id=1 -d name=product -d price=99.8
# Product{id='1', name='product', price='99.8'} 更新成功
```
### upload
```bash
echo hello > foo
curl -m1 "$host/product/upload" -F file=@foo
# upload success filename: foo
curl -m1 "$host/product/upload" -F "file=@foo;filename=xyz"
# upload success filename: xyz

### 
# 如果媒体类型不对，500  
# Current request is not of type [org.springframework.web.multipart.Multi
curl -m1 "$host/product/upload"  -X POST

# 如果没有上传文件，500
curl -m1 "$host/product/upload"  -X POST -F name=Jack
# ：：No message available
```
### 总结（旁路）
和这个测试无关，和Spring框架有关。

@RestController 用在类上，相当于对类中的每个方法使用 @ResponseBody 方法，效果是：
方法的返回值声明为对象，框架会以 application/json 的格式返回，框架负责转为json字符串。
方法的返回值声明为字符串，框架会以 text/plain 的格式返回，不做转换。

@RequestMapping 指定请求方法，则对应快捷注解 @GetMapping，@PostMapping，@DeleteMapping。

方法形参不加注解，则期望请求类型为表单，方法形参自动对应表单参数；如果形参类型为实体类，则自动封装为实体类。
如果形参使用 @RequestBody 注解，则期望请求类型为 appliation/json，自动封装为实体类。
可以在url中使用占位符 "/path/{id}"，然后在形参中使用 @PathVariables 注解提取路径参数。

接收上传的文件，使用 POST 方法，使用 MultipartRequest 形参。
