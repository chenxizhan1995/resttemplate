package com.one.learn.resttemplate;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;

/**
 * 实战：实现如下三种媒体类型的请求的发送
 * - application/x-www-form-urlencoded 对应 MultiValueMap
 * - application/json 对应POJO
 * - multipart/form-data 对应何种类型？
 */
public class RestTemplateDemo3 {
    /**
     * 在本地启动 Fiddler 程序，该程序提供 http 代理，地址为 localhost:8888
     * 它会对经过此代理的HTTP请求抓包，方便学习
     */
    @Before
    public void setProxy() {
        System.out.println("set proxy");
        System.setProperty("http.proxyHost", "localhost");
        System.setProperty("http.proxyPort", "8888");
    }

    /**
     * 名值对发送
     * 1. 使用 MultiValueMap<String, String>  做请求体，自动按名值对处理
     */
    @Test
    public void postNv() {
        RestTemplate template = new RestTemplate();

        MultiValueMap<String, String> mvs = new LinkedMultiValueMap<>();
        mvs.add("name", "Jack");

        String res = template.postForObject("http://httpbin.org/post", mvs, String.class);
        System.out.println(res);
    }
    /*
     * json，已经见识过了，凡是POJO类，它默认一律按JSON处理。
     * 特别指出，String 字符串，默认按 text/plain 处理。
     */


    /**
     * multipart 请求
     * 1. 一直没有找到令人满意的解答。
     * 2. 勉强可以接受的解答，
     * 手动设置 multipart/form-data 首部
     * 用 MultiValueMap<String, FileSystemResource> 做请求体
     * <p>
     * 此示例，表明，multipart 下的
     * - FileSystemResource 会自动补充文件上传相关的首部
     * - 字符串，会按纯文本处理
     */

    @Test
    public void postFile() {
        RestTemplate template = new RestTemplate();

        FileSystemResource resource = new FileSystemResource("src/test/resources/data");

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);
        body.add("comment", "This is a binary file");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, ?>> entity = new HttpEntity<>(body, headers);
        String res = template.postForObject("http://httpbin.org/post", entity, String.class);
    }

    /**
     * 接上，FileSystemResource 方式上传文件毕竟不够通用
     * <p>
     * ClassPathResource 呢？
     * 仍然能按文件处理 Content-Disposition: form-data; name="file2"; filename="data"
     * <p>
     * 它与FileSystemResource 都有 filename 方法
     */
    @Test
    public void postFile2() {
        RestTemplate template = new RestTemplate();

        Resource resource = new ClassPathResource("/data");
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file2", resource);
        body.add("comment", "This is the same binary file");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, ?>> entity = new HttpEntity<>(body, headers);
        String res = template.postForObject("http://httpbin.org/post", entity, String.class);
    }

    /**
     * 承上，再通用一些，InputStream 呢？
     * 哈，报错了 org.springframework.http.converter.HttpMessageNotWritableException: Could not write request:
     * no suitable HttpMessageConverter found for request type [java.io.BufferedInputStream]
     */
    @Test
    public void postFile3() throws IOException {
        RestTemplate template = new RestTemplate();

        Resource resource = new ClassPathResource("/data");
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file2", resource.getInputStream());
        body.add("comment", "This is the same binary file");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, ?>> entity = new HttpEntity<>(body, headers);
        String res = template.postForObject("http://httpbin.org/post", entity, String.class);
    }

    /**
     * 再来一个，如果是 InputStreamResource 呢
     * Ans: 那就缺少了 filename 字段
     *
     * @throws IOException
     */
    @Test
    public void postFile4() throws IOException {
        RestTemplate template = new RestTemplate();

        Resource resource = new ClassPathResource("/data");
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file2", new InputStreamResource(resource.getInputStream()));
        body.add("comment", "This is the same binary file");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, ?>> entity = new HttpEntity<>(body, headers);
        String res = template.postForObject("http://httpbin.org/post", entity, String.class);
    }

    /**
     * 有个 ContentDisposition 类，是干嘛的？
     * 貌似找到了
     *
     * @throws IOException
     */
    @Test
    public void postFile5() throws IOException {
        RestTemplate template = new RestTemplate();

        HttpEntity<Resource> file = null;
        {
            ContentDisposition contentDisposition = ContentDisposition.formData()
                    .filename("name in post")
                    .name("a file")
                    .build();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentDisposition(contentDisposition);

            Resource body = new InputStreamResource(new ClassPathResource("/data").getInputStream());
            file = new HttpEntity<>(body, headers);
        }
        MultiValueMap<String, Object> mv = new LinkedMultiValueMap<>();
        mv.add("file", file);
        mv.add("comment", "this is a file");
        HttpEntity<MultiValueMap<String, Object>> mainBody = new HttpEntity<>(mv);

        String res = template.postForObject("http://httpbin.org/post", mainBody, String.class);
    }
    /*
     * 总是，发送文件，太灵活了，让我有些不知所措。
     */

    /**
     * Q. 是否 HttpEntity 的嵌套自然生成 multipart 首部？
     * 是的
     *
     * @throws IOException
     */

    @Test
    public void postFile6() throws IOException {
        RestTemplate template = new RestTemplate();

        HttpEntity<String> part1 = new HttpEntity<>("hello");

        MultiValueMap<String, Object> mv = new LinkedMultiValueMap<>();
        mv.add("part1", part1);
        mv.add("comment", "this is a file");

        HttpEntity<MultiValueMap<String, Object>> mainBody = new HttpEntity<>(mv);

        String res = template.postForObject("http://httpbin.org/post", mainBody, String.class);
    }

    /**
     * 有个 ContentDisposition 类，会自动进行 url 编码吗？
     * Ans：不会
     * 那我要它何用？
     * 唯一的用途大概是不用手动写入首部的名称了。
     * 可是，如果我理解这个首部的用法，记下对应的首部名称，也不难啊！！！
     *
     * @throws IOException
     */
    @Test
    public void postFile7() throws IOException {
        RestTemplate template = new RestTemplate();

        HttpEntity<Resource> file = null;
        {
            ContentDisposition contentDisposition = ContentDisposition.formData()
                    .filename("中文文件名")
                    .name("a file")
                    .build();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentDisposition(contentDisposition);

            Resource body = new InputStreamResource(new ClassPathResource("/data").getInputStream());
            file = new HttpEntity<>(body, headers);
        }
        MultiValueMap<String, Object> mv = new LinkedMultiValueMap<>();
        mv.add("file", file);
        HttpEntity<MultiValueMap<String, Object>> mainBody = new HttpEntity<>(mv);

        String res = template.postForObject("http://httpbin.org/post", mainBody, String.class);
    }
    //---------------------------------
    // 发送文件的各种姿势

    /**
     * 直接发送 File 对象
     * Ans: 不行，实际发送的是文件路径
     */
    @Test
    public void postFile8() {
        RestTemplate template = new RestTemplate();
        File file = new File("src/test/resources/data");
        String res = template.postForObject("http://httpbin.org/post", file, String.class);
    }

    /**
     * 发送 FileSystemResource
     * Ans:不行，application/octet-stream
     */
    @Test
    public void postFile9() {
        RestTemplate template = new RestTemplate();
        File file = new File("src/test/resources/data");
        String res = template.postForObject("http://httpbin.org/post", new FileSystemResource(file), String.class);
    }

    /**
     * 指定 multipart/form-data 首部，+ File 对象
     * Ans：不行，No HttpMessageConverter for java.io.File and content type "multipart/form-data"
     */
    @Test
    public void postFile10() {
        RestTemplate template = new RestTemplate();
        File file = new File("src/test/resources/data");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity entity = new HttpEntity(file, headers);
        String res = template.postForObject("http://httpbin.org/post", entity, String.class);
    }

    /**
     * 指定 multipart/form-data 首部，+ FileSystemResource 对象
     * Ans: 请求报文不对，只是首部有变化，请求体没有自动调整，八成是预期开发者自动处理请求体？
     */
    @Test
    public void postFile11() {
        RestTemplate template = new RestTemplate();
        File file = new File("src/test/resources/data");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity entity = new HttpEntity(new FileSystemResource(file), headers);
        String res = template.postForObject("http://httpbin.org/post", entity, String.class);
    }

    /**
     * 指定 multipart/form-data 首部，+ Mv 封装的 FileSystemResource 对象
     * Ans: 可以
     */
    @Test
    public void postFile12() {
        RestTemplate template = new RestTemplate();
        File file = new File("src/test/resources/data");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Resource> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("file", new FileSystemResource(file)  );

        HttpEntity entity = new HttpEntity(multiValueMap, headers);
        String res = template.postForObject("http://httpbin.org/post", entity, String.class);
    }

    /**
     * 指定 multipart/form-data 首部，+ Mv 封装的 File 对象
     * Ans: 不行。虽然拆分了，但是按照 application/json 发送了文件路径
     */
    @Test
    public void postFile13() {
        RestTemplate template = new RestTemplate();
        File file = new File("src/test/resources/data");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("file", file );

        HttpEntity entity = new HttpEntity(multiValueMap, headers);
        String res = template.postForObject("http://httpbin.org/post", entity, String.class);
    }
}


