package com.one.learn.resttemplate;

import com.sun.xml.internal.ws.resources.StreamingMessages;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * 关注 RestTemplate 的 postForObject() 方法的 request 参数
 * 不同POJO类型，RestTemplate 是如何处理的？
 */
public class RestTemplateDemo2 {
    @BeforeAll
    public static void setProxy() {
        System.out.println("setup");
        System.setProperty("http.proxyHost", "localhost");
        System.setProperty("http.proxyPort", "8888");
    }

    /**
     * 传入字符串，默认按 text/plain 发送
     */
    @Test
    public void test1() {
        RestTemplate client = new RestTemplate();
        String res = client.postForObject("http://httpbin.org/post", "hello", String.class);
        System.out.println(res);
    }

    /**
     * 就和手动指定 text/plain 首部一样
     */
    @Test
    public void test2() {
        RestTemplate client = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<String> reqEntity = new HttpEntity<>("hello, world", headers);

        String res = client.postForObject("http://httpbin.org/post",
                reqEntity, String.class);
        System.out.println(res);
    }

    /**
     * json 字符串+ application/json 请求，会如何
     * Ans: 就发出了正常的 JSON 请求啊
     */
    @Test
    public void test3() {
        RestTemplate client = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        HttpEntity<String> reqEntity = new HttpEntity<>("{ \"name\": \"Jack\"}", headers);

        String res = client.postForObject("http://httpbin.org/post",
                reqEntity, String.class);
        System.out.println(res);
    }

    /**
     * Map 做请求，+ application/x-www-form-urlencoded 请求，会报错
     * org.springframework.web.client.RestClientException:
     * No HttpMessageConverter for [java.util.HashMap]
     * and content type [application/x-www-form-urlencoded]
     */
    @Test
    public void test4() {
        RestTemplate client = new RestTemplate();
        Map<String, String> body = new HashMap<>();
        body.put("name", "Jack");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<Map<String, String>> reqEntity = new HttpEntity<>(body, headers);

        String res = client.postForObject("http://httpbin.org/post",
                reqEntity, String.class);
        System.out.println(res);
    }

    /***
     * 发送表单名值对，得用 MultiValueMap<String, String> 作为请求体类型。
     * RestTemplate 会自动使用 Content-Type: application/x-www-form-urlencoded 首部
     * 也会自动对 name 和 value 进行 url 编码。
     * 当然，手动写上这个首部也没问题
     */
    @Test
    public void test5() {
        RestTemplate template = new RestTemplate();
        MultiValueMap<String, String> mvm = new LinkedMultiValueMap<>();
        mvm.add("name", "Jack Tang");
        mvm.add("name hello %", "Jack%&Tang");
        RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
                .post("http://httpbin.org/post")
                .body(mvm);
        String res = template.exchange(requestEntity, String.class).getBody();
        System.out.println(res);
    }

    /**
     * 如果非要指定其它 Content-Type 呢？
     * Ans: 如下，会报错的
     * org.springframework.web.client.RestClientException:
     * No HttpMessageConverter for org.springframework.util.LinkedMultiValueMap
     * and content type "text/plain"
     * <p>
     * 指定 application/json 可以的，只是会把每个值都按数组处理了
     */
    @Test
    public void test6() {
        RestTemplate template = new RestTemplate();
        MultiValueMap<String, String> mvm = new LinkedMultiValueMap<>();
        mvm.add("name", "Jack Tang");
        RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
                .post("http://httpbin.org/post")
                // .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(mvm);
        String res = template.exchange(requestEntity, String.class).getBody();
        System.out.println(res);
    }

    /**
     * 已经编码过的会，也会编码……，要当心
     */
    @Test
    public void test7() {
        RestTemplate template = new RestTemplate();
        MultiValueMap<String, String> mvm = new LinkedMultiValueMap<>();
        mvm.add("name+hello+%25", "Jack%25%26Tang");
        RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
                .post("http://httpbin.org/post")
                .body(mvm);
        String res = template.exchange(requestEntity, String.class).getBody();
        System.out.println(res);
    }

    @Test
    public void test() {
        RestTemplate client = new RestTemplate();

        final String urlGetMaxIr = "http://172.16.12.133:8088/api/System/GetDepositInterestRate";

        MultiValueMap<String, String> mvm = new LinkedMultiValueMap<>();
        mvm.add("ClientID", "90685A0D-7A4C-40D2-BE08-FCAA6C5C92C4");

        ResponseEntity<String> response = client.exchange(urlGetMaxIr, HttpMethod.POST
                , new HttpEntity<>(mvm, null)
                , String.class);
        System.out.println(response.getBody());
    }

    /**
     * RestTemplate 发送带有body的get请求
     */
    @Test
    public void test10() {
        final String url = "http://172.16.12.133:8088/api/System/GetDepositInterestRate";
        MultiValueMap<String, String> mvm = new LinkedMultiValueMap<>();
        mvm.add("ClientId", "90685A0D-7A4C-40D2-BE08-FCAA6C5C92C4");

        RestTemplate client = new RestTemplate(new CustomHttpComponentsClientHttpRequestFactory());
        String res = client.exchange(url, HttpMethod.GET, new HttpEntity<>(mvm), String.class).getBody();
        System.out.println(res);
    }

    private static final class CustomHttpComponentsClientHttpRequestFactory extends HttpComponentsClientHttpRequestFactory {

        @Override
        protected HttpUriRequest createHttpUriRequest(HttpMethod httpMethod, URI uri) {

            if (HttpMethod.GET.equals(httpMethod)) {
                return new HttpEntityEnclosingGetRequestBase(uri);
            }
            return super.createHttpUriRequest(httpMethod, uri);
        }
    }

    private static final class HttpEntityEnclosingGetRequestBase
            extends HttpEntityEnclosingRequestBase {

        public HttpEntityEnclosingGetRequestBase(final URI uri) {
            super.setURI(uri);
        }

        @Override
        public String getMethod() {
            return HttpMethod.GET.name();
        }
    }

    /**
     * 如果直接拿 Resource 做请求体，会如何？
     * Ans: Content-Type: text/plain
     *
     */
    @Test
    public void postResource(){
        RestTemplate template = new RestTemplate();

        Resource req = new ClassPathResource("/foo.txt");
        String res = template.postForObject("http://httpbin.org/post", req, String.class);
    }

    /**
     * 承上，如果使用二进制文件呢？
     * Answer：Content-Type: application/octet-stream
     * 这其中，规律不明啊
     */
    @Test
    public void postResource2(){
        RestTemplate template = new RestTemplate();

        Resource req = new ClassPathResource("/data");
        String res = template.postForObject("http://httpbin.org/post", req, String.class);
    }

    /**
     * 承上，使用 FileSystemResource，仍旧是  Content-Type: application/octet-stream
     */
    @Test
    public void postResource3(){
        RestTemplate template = new RestTemplate();
        FileSystemResource resource = new FileSystemResource("src/test/resources/data");
        String res = template.postForObject("http://httpbin.org/post", resource, String.class);
    }

}
