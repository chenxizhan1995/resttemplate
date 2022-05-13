package com.one.learn.resttemplate;

import org.junit.Test;
import org.springframework.http.HttpHeaders;

public class Demo {
    /**
     * 听说 org.springframework.http.HttpHeaders 类不区分首部name的大小写
     */
    @Test
    public void test1(){
        HttpHeaders headers = new HttpHeaders();
        headers.add("foo", "Jack");
        headers.add("Foo", "Jack");
        headers.add("Bar", "Jack");
        headers.add("bar", "Jack");
        System.out.println(headers);
        // [foo:"Jack", "Jack", Bar:"Jack", "Jack"]
    }
}
