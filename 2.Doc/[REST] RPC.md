<a name="e79a2135"></a>
# Getting started【REST Clinet】
<a name="a12b88a4"></a>
## [0] 概述
用于分布式系统下的远程调用
<a name="e4809c63"></a>
## [1] 添加依赖
pom.xml
```xml
<dependency>
    <groupId>cn.rylan</groupId>
    <artifactId>spring-cloud-starter-StellaLou</artifactId>
    <version>1.0.0</version>
</dependency>
```
<a name="09f3e881"></a>
## [2] 修改配置文件
application.yaml
```yaml
server:
  port: 5000

spring:
  application:
    name: customer
  cloud:
    #注册中心配置
    zookeeper:
      connect-string: 127.0.0.1:2181
      discovery:
        prefer-ip-address: true
    #远程调用配置
    stellalou:
      rest:
        enable: true
        balancer: random  #负载均衡配置 [随机random - 轮询roundRobin]
```
application.properties
```properties
server.port=5000
spring.application.name=customer

spring.cloud.zookeeper.connect-string=127.0.0.1:2181
spring.cloud.zookeeper.discovery.prefer-ip-address=true
spring.cloud.stellalou.rest.enable=true
#负载均衡配置 [随机random - 轮询roundRobin]
spring.cloud.stellalou.rest.balancer=random
```
负载均衡如果没有配置，默认是random随机
<a name="97851ca0"></a>
## [3] 使用注解

```java
//启动类
@EnableRestHttpClinet(basePackages = "cn.customer.rest")

//mapping 
@HttpRequestMapping(uri ="" , type = "${HTTP.GET 或 HTTP.POST}") 
```

```markdown
@EnableRestHttpClinet(basePackages = "")
basePackages参数为专门编写rest客户端的包

DEMO-CUSTOMER
└─src
    ├─main
    │  ├─java
    │  │  └─cn
    │  │      └─customer
    │  │          ├─config
    │  │          ├─controller
    │  │          ├─rest 
    │  │          └─service
    │  └─resources
    └─test
```
<a name="e9a5e91c"></a>
## [4] 编写REST客户端
编写Provider应用注册到注册中心（zookeeper 或 nacos）并提供HTTP REST接口<br />***注**：如果使用zookeeper注册中心需要在配置文件中指定`prefer-ip-address: true`来保证元数据是以地址，端口的形式注册。<br />![image.png](https://cdn.nlark.com/yuque/0/2023/png/26986120/1680661385283-e91108b7-b0b7-4afd-87be-853db147ed0e.png#averageHue=%23f8f8f7&clientId=u8c8c49e0-f022-4&from=paste&height=825&id=u22f1927a&name=image.png&originHeight=921&originWidth=1589&originalType=binary&ratio=1.1166666746139526&rotation=0&showTitle=false&size=92110&status=done&style=none&taskId=uacf6633c-22e2-4462-a239-b17bb495129&title=&width=1422.9850644995201)
```yaml
  cloud:
    zookeeper:
      connect-string: 127.0.0.1:2181
      discovery:
        prefer-ip-address: true
```
```java
@RestController
public class TController {

    @GetMapping("/provider/user/id/{id}")
    public User getUserById(@PathVariable("id") Integer id) {
        if (id != 1001) {
            return null;
        }
        return new User(1001, "alex", "123456");
    }
}
```

编写Customer调用Provider的HTTP REST接口，服务注册同上。
```java
@RestHttpClient(serviceName = "provider")
public interface UserClient {

    @HttpRequestMapping(uri = "/provider/user/id/{id}", type = HTTP.GET)
    User getUserById(@PathVariable("id") Integer id);
}
```

```java
@RestController
public class TController {

   @Autowired
   private UserClient userClient;

    @GetMapping("/customer/user/id/{id}")
    public User getUserInfo(@PathVariable("id") Integer id) {
       return userClient.getUserById(id);
    }
}
```

<a name="9ace55d6"></a>
## [5] 自定义请求头
重写请求拦截器，对请求模板进行添加请求头，例如让restClient发送的请求携带 "当前请求的" cookie数据
```java
@Configuration
public class BeanConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                var requestAttributes = RequestContextHolder.getRequestAttributes();
                var request = ((ServletRequestAttributes) requestAttributes).getRequest();
                var cookie = request.getHeader("Cookie");
                var headers = new HttpHeaders();
                headers.add("Cookie", cookie);
                headers.setAcceptCharset(Collections.singletonList(StandardCharsets.UTF_8));
                headers.setContentType(MediaType.APPLICATION_JSON);
                template.setHeaders(headers);
            }
        };
    }
}
```
<a name="rWmsd"></a>
## [6] 日志打印
启动类扫描包<br />将代理对象注入到IOC容器中<br />![log1.png](https://cdn.nlark.com/yuque/0/2023/png/26986120/1680662926221-0b232195-f245-4679-a276-480d622c3f25.png#averageHue=%23292d30&clientId=u941b1944-21c1-4&from=paste&height=804&id=uaf1449f2&name=log1.png&originHeight=898&originWidth=1916&originalType=binary&ratio=1.1166666746139526&rotation=0&showTitle=false&size=263874&status=done&style=none&taskId=ufbea00fa-dbf4-4e8a-ba76-4658f8d010d&title=&width=1715.820883310938)<br />调用URL<br />![log2.png](https://cdn.nlark.com/yuque/0/2023/png/26986120/1680663025793-8ad183f6-ba3e-4f61-9ba6-19a555336c5c.png#averageHue=%23282d2f&clientId=u941b1944-21c1-4&from=paste&height=595&id=u42c9b89d&name=log2.png&originHeight=664&originWidth=1891&originalType=binary&ratio=1.1166666746139526&rotation=0&showTitle=false&size=223337&status=done&style=none&taskId=u060cccc1-c1fb-45f9-87cb-30725f7aedf&title=&width=1693.4328237687807)

<a name="JOl3t"></a>
## [7] 懒加载
启动时花了14s，这是相当慢了，但并非是自己通过spring扩展点自定义向IOC注入Bean的原因<br />对照组就是在自动注入的字段上加上`@Lazy`注解，让其支持懒加载，在被调用时才会注入，然而启动还是14s，所以这和我没关系。。。<br />![image.png](https://cdn.nlark.com/yuque/0/2023/png/26986120/1680663336539-7bbbcf46-58f2-4ab2-9e81-22ddc54693a3.png#averageHue=%23292c2f&clientId=u941b1944-21c1-4&from=paste&height=691&id=uc964c5df&name=image.png&originHeight=772&originWidth=1897&originalType=binary&ratio=1.1166666746139526&rotation=0&showTitle=false&size=178244&status=done&style=none&taskId=u238b31e1-2ff9-4335-8b27-675e564f2b6&title=&width=1698.8059580588983)<br />即使和我没关系，那能不能也让其本身就支持懒加载？当然可以，只需在在遍历`BeanDefinitions`时，让bean的定义成支持懒加载就行，但需要注意的是如果这个bean默认是单例注入进IOC的，此方法会失效。<br />![image.png](https://cdn.nlark.com/yuque/0/2023/png/26986120/1680663497438-39d01569-5e54-4ea1-85cb-5cb17f45ecc3.png#averageHue=%2326282a&clientId=u941b1944-21c1-4&from=paste&height=596&id=u0ca582cd&name=image.png&originHeight=666&originWidth=1253&originalType=binary&ratio=1.1166666746139526&rotation=0&showTitle=false&size=114525&status=done&style=none&taskId=ua95c3941-0aea-4b0d-9ab1-80d5bd9b6c3&title=&width=1122.0895442529256)<br />![image.png](https://cdn.nlark.com/yuque/0/2023/png/26986120/1680663718182-ef92beba-8c8b-4d1f-a745-54cf407063f2.png#averageHue=%23242527&clientId=u941b1944-21c1-4&from=paste&height=625&id=DlXjn&name=image.png&originHeight=698&originWidth=1369&originalType=binary&ratio=1.1166666746139526&rotation=0&showTitle=false&size=109426&status=done&style=none&taskId=ud4dd96db-8f17-4610-be6b-2bff859beb3&title=&width=1225.9701405285355)<br />ChatGPT的回答<br />![image.png](https://cdn.nlark.com/yuque/0/2023/png/26986120/1680664142621-5aab045e-ca41-4f08-a391-3dbf31fd2ea3.png#averageHue=%236790ab&clientId=u57859106-2150-4&from=paste&height=821&id=u0f90ad8c&name=image.png&originHeight=917&originWidth=1920&originalType=binary&ratio=1.1166666746139526&rotation=0&showTitle=false&size=164903&status=done&style=none&taskId=u0f48b7da-9fb4-4870-8e32-09045e7e131&title=&width=1719.4029728376831)
<a name="yu9Db"></a>
## [8] 杂谈
年前从秋招的实习公司走了回家呆着，闲着的时候在知乎上看到这样一个问题，feign算不算RPC，里面的回答离了大谱，但也觉得挺有意思，因为之前写过类似于dubbo这种RPC的小轮子，所以对构建REST客户端这种也很感兴趣，于是某个晚上简单的实现了下（叫closefeign），虽然是那么个意思但是对结果不是很满意，于是第二天fork了openfeign的仓库想看看（抄）一下实现，回退到第一个版本刚准备研究下，就被拉出去喝酒头疼了两天，后面就不太想花时间再研究了，本着正好还能往简历个人项目经历上再填几句的想法，就按照之前的思路一直写了（虽然代码实现的比较笨）。<br />![image.png](https://cdn.nlark.com/yuque/0/2023/png/26986120/1680662210831-433bf4a7-83b4-4437-a029-35c224b38366.png#averageHue=%2311161d&clientId=u8c8c49e0-f022-4&from=paste&height=777&id=t3voo&name=image.png&originHeight=868&originWidth=1902&originalType=binary&ratio=1.1166666746139526&rotation=0&showTitle=false&size=129007&status=done&style=none&taskId=u388685b8-256a-48f9-b44f-958acaba9a4&title=&width=1703.2835699673299)

![image.png](https://cdn.nlark.com/yuque/0/2023/png/26986120/1680662049045-9947035d-a25d-4681-9f51-962b7940155d.png#averageHue=%230f141b&clientId=u8c8c49e0-f022-4&from=paste&height=774&id=ud497fa89&name=image.png&originHeight=864&originWidth=1889&originalType=binary&ratio=1.1166666746139526&rotation=0&showTitle=false&size=100804&status=done&style=none&taskId=u0966db35-2b6f-4301-a837-bb45c8417c0&title=&width=1691.6417790054081)

![image.png](https://cdn.nlark.com/yuque/0/2023/png/26986120/1680662015208-00e974a4-db28-44d6-80e2-3ed64b41d851.png#averageHue=%2310151b&clientId=u8c8c49e0-f022-4&from=paste&height=783&id=uca116150&name=image.png&originHeight=874&originWidth=1893&originalType=binary&ratio=1.1166666746139526&rotation=0&showTitle=false&size=106549&status=done&style=none&taskId=u9807cb2c-e054-4f00-b30f-0bf5b1277f4&title=&width=1695.2238685321533)

