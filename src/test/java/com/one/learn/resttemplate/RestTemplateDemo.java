package com.one.learn.resttemplate;

import com.fasterxml.jackson.databind.JsonNode;
import org.assertj.core.internal.bytebuddy.asm.Advice;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** RestTemplate 基础，关注 getForObject(), getForEntity(), postForObject(), postForEntity() 的用法。 */
public class RestTemplateDemo {

    /**
     * 最简单的是 getForObject(url, String.class) ---> String
     */
    @Test
    public void test1() {
        RestTemplate client = new RestTemplate();
        String res = client.getForObject("http://httpbin.org/get", String.class);
        System.out.println(res);
        // 响应结果：test/resources/resttemplate-demo/test1.json
    }

    /**
     * 方法签名中的 uriVariables 是什么？
     * <T> T 	getForObject(String url, Class<T> responseType, Object... uriVariables)
     */
    @Test
    public void test2() {
        RestTemplate client = new RestTemplate();
        String res = client.getForObject("http://httpbin.org/anything/{school}/{name}/",
                String.class, "DingTao", "Jack");
        // 实际的请求url会变成
        // "http://httpbin.org/anything/DingTao/Jack"
        System.out.println(res);
    }

    @Test
    public void test3() {
        RestTemplate client = new RestTemplate();
        Map<String, Object> pvs = new HashMap<>();
        pvs.put("school", "DingTao");
        pvs.put("name", "jack");
        pvs.put("id", "10086");
        String res = client.getForObject("http://httpbin.org/anything/{school}/{name}?id={id}",
                String.class, pvs);
        // 实际的请求url会变成 http://httpbin.org/anything/DingTao/jack?id=10086
        System.out.println(res);

    }

    /**
     * 当响应类型为 application/json 时，可以快速把响应结果解析为指定的实体类。
     */
    @Test
    public void test4() {
        RestTemplate client = new RestTemplate();
        JsonNode json = client.getForObject("http://httpbin.org/get", JsonNode.class);
        System.out.println(json);
    }

    @Test
    public void test5() {
        RestTemplate client = new RestTemplate();
        // 若响应结果为 400，会抛出异常，不论 Class 参数为何
        // org.springframework.web.client.HttpClientErrorException$BadRequest: 400 BAD REQUEST

        // JsonNode json = client.getForObject("http://httpbin.org/status/400", JsonNode.class);
        // String res = client.getForObject("http://httpbin.org/status/400", String.class);

        // 201 则返回 null
        String res = client.getForObject("http://httpbin.org/status/201", String.class);
        System.out.println(res);
        JsonNode json = client.getForObject("http://httpbin.org/status/201", JsonNode.class);
        System.out.println(json);

    }

    /**
     * 认识 getForEntity()
     */
    @Test
    public void test6() {
        RestTemplate client = new RestTemplate();
        ResponseEntity<JsonNode> responseEntity = client.getForEntity("http://httpbin.org/get", JsonNode.class);
        System.out.println(responseEntity.getStatusCode());
        HttpHeaders headers = responseEntity.getHeaders();
        // 以良好的格式打印首部，同时看看状态行是否包含在首部内
        // 不会
        System.out.println("headers：==================================");
        for (String name : headers.keySet()) {
            System.out.println(name + ": " + headers.get(name));
        }
        System.out.println("首部结束：===================================");
        JsonNode json = responseEntity.getBody();
        System.out.println(json);
    }

    /**
     * getForEntity() 遇上 500 响应，会抛出异常吗？就是，我还有机会获取 ResponseEntity 吗？
     * Ans: 没有机会，发起请求的时候就直接抛出了异常
     */
    @Test
    public void test7() {
        RestTemplate client = new RestTemplate();
        try {
            ResponseEntity<String> responseEntity =
                    client.getForEntity("http://httpbin.org/status/500", String.class);
            System.out.println(responseEntity.getStatusCode());
            System.out.println(responseEntity.getBody());
        } catch (Exception e) {
            System.out.println("抛出了异常");
            e.printStackTrace();
        }
    }

    @Test
    public void test8() {
        RestTemplate client = new RestTemplate();
        try {
            ResponseEntity<Object> responseEntity =
                    client.getForEntity("http://httpbin.org/status/500", Object.class);
            System.out.println(responseEntity.getStatusCode());
            System.out.println(responseEntity.getBody());
        } catch (Exception e) {
            System.out.println("抛出了异常");
            e.printStackTrace();
        }
    }

    /** 无论遇到什么错误，都正常获取 ResponseEntity
     *
      */
    @Test
    public void test9(){
        RestTemplate client = new RestTemplate();
        client.setErrorHandler(new ErrorHandler());
        ResponseEntity<String> responseEntity = client.getForEntity("http://httpbin.org/status/400", String.class);
        HttpHeaders headers = responseEntity.getHeaders();
        System.out.println("===================================");
        for (String name : headers.keySet()) {
            System.out.println(name + ": " + headers.get(name));
        }
        System.out.println("====================================");
        System.out.println(responseEntity.getBody());
    }
    static class ErrorHandler extends DefaultResponseErrorHandler {
        @Override
        public boolean hasError(ClientHttpResponse response) throws IOException {
            return false;
        }
    }

    /**
     * 1. post 请求中的 requsest 是否可以为空？
     *  Ans：可以。用 null 就行
     *  Q. 空字符串""可以吗？
     * 2. post 方法默认的 MIME 类型是什么？
     * Ans: application/x-www-form-urlencoded
     */
    @Test
    public void post1(){
        String res = new RestTemplate().postForObject("http://httpbin.org/post", null, String.class);
        System.out.println(res);
        // 发送的请求，感兴趣的首部
        // "Content-Type": "application/x-www-form-urlencoded",
        // "Content-Length": "0"
    }

    /**
     * 请求为字符串的时候
     */
    @Test
    public void post2(){
        String res = new RestTemplate()
                .postForObject("http://httpbin.org/post", "", String.class);
        System.out.println(res);
    }

    /**
     * postForObject 的 request 参数为Map对象时，
     * 按json处理
     */
    @Test
    public void post3(){
        Map<String, Object> request = new HashMap<>();
        request.put("name", "Jack");
        request.put("age", 12);

        String res = new RestTemplate()
                .postForObject("http://httpbin.org/post", request, String.class);
        System.out.println(res);
    }
    @Test
    public void post4(){
        Person person = new Person();
        person.name = "Jack";
        person.birthday = new Date(8195616000L);

        String res = new RestTemplate()
                .postForObject("http://httpbin.org/post", person, String.class);
        System.out.println(res);
    }
    static class Person{
        String name;
        Date birthday;

        public String getName() {
            return name;
        }

        public Date getBirthday() {
            return birthday;
        }
    }

}
