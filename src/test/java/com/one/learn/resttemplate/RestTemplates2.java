package com.one.learn.resttemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.one.learn.resttemplate.bean.Product;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * <p> Description: 仿写 RestTemplateTests </p>
 */
public class RestTemplates2 {

    RestTemplate restTemplate;
    String host = "http://chenxizhan.top/httpbin";
    String host2 = "http://localhost:8080";

    @Before
    public void setRestTemplate() {
        // 可以直接 new 对象
        // this.restTemplate = new RestTemplate();

        // 但是通过提供 SimpleClientHttpRequestFactory 对象，可以设置超时时间
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2_000);
        factory.setReadTimeout(5_000);
        this.restTemplate = new RestTemplate(factory);
    }

    @Test
    public void testGet() {
        String url = host2 + "/product/get-product-1";
        // 最基本的用法，直接 getForObject(url, class)
        // 1. 熟悉 url 和 class 参数的使用
        // 2. ResponseEntity 的简单用法
        {
            String ret = restTemplate.getForObject(url, String.class);
            System.out.println(ret);
            // {"id":1,"name":"ProductA","price":6666.0}
        }
        {
            // Product.class 参数可以直接把json串反序列化为实体对象
            Product product = restTemplate.getForObject(url, Product.class);
            System.out.println(product);
            // Product{id='1', name='ProductA', price='6666.0'}
            Assert.notNull(product, "对象为空");
        }
        {
            //方式三：GET 方式获取包含 Product 实体对象 的响应实体 ResponseEntity 对象,用 getBody() 获取
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            Assert.isTrue(response.hasBody(), "没有消息体");
            System.out.println(response.getBody());
            // {"id":1,"name":"ProductA","price":6666.0}
        }
    }

    @Test
    public void testGet2() {
        // 1. url变量: url 中可以设置占位符，后面的变量按位置赋值
        // 实际发送了 GET "http://chenxizhan.top/get" 的请求
        {
            String ret = restTemplate.getForObject(host + "/{path}", String.class, "get");
            System.out.println(ret);
        }

        {
            String ret = restTemplate.getForObject(host2 + "/product/get-product-2?id={id}", String.class, 12);
            System.out.println(ret);
            // {"id":12,"name":"ProductC","price":6666.0}
        }
    }
    @Test(expected = HttpClientErrorException.class)
    public void testPost(){
        // POST 发送JSON数据
        String url = host2 + "/product/post-product-2";

        // 直接传入实体类，自然转为JSON发送
        {
            Product param = new Product();
            param.setId(12);
            param.setName("posted");
            param.setPrice(BigDecimal.valueOf(999.9));

            String result = restTemplate.postForObject(url, param, String.class);
            System.out.println(result);
            // Product{id='12', name='posted', price='999.9'}
        }
        // 传入 Map 对象
        {
            Map<String, Object> param = new HashMap<>();
            param.put("id", 12);
            param.put("name", "post");
            param.put("price", BigDecimal.valueOf(999.9));
            String result = restTemplate.postForObject(url, param, String.class);
            System.out.println(result);
            // Product{id='12', name='post', price='999.9'}
        }
        // 传入字符串
        {
            String request = "{ \"id\":1, \"name\": \"product\", \"price\": 99.8}";
            String result = restTemplate.postForObject(url, request, String.class);
            System.out.println(result);
            //  服务器端响应：415 UNSUPPORTED_MEDIA_TYPE
        }
    }

    /**
     * post 方式发送表单数据
     */
    @Test
    public void postForm(){
        String url = "http://httpbin.org/post";
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("name", "Jack");
        map.add("name", "Jack");
        String ret = restTemplate.postForObject(url, map, String.class);
        System.out.println(ret);
    }
    @Test
    public void head(){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = restTemplate.headForHeaders("https://httpbin.org/get");
        System.out.println(httpHeaders.getContentType());
        org.junit.Assert.assertTrue(httpHeaders.getContentType().includes(MediaType.APPLICATION_JSON));
    }
    @Test
    public void test2(){
        RestTemplate restTemplate = new RestTemplate();
        JsonNode json = restTemplate.getForObject("https://httpbin.org/get", JsonNode.class);
        System.out.println(json);
        System.out.println(json.get("url").asText());
    }

}
