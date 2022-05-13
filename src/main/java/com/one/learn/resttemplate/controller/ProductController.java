package com.one.learn.resttemplate.controller;

import com.one.learn.resttemplate.bean.Product;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;

import java.math.BigDecimal;

/**
 * @author One
 * @Description Product 控制器
 */
@RequestMapping("/product")
@RestController
public class ProductController {
    /**
     * 返回默认的产品
     * @return
     */
    @GetMapping({"/get-product-1", "/get_product1"})
    public Product get_product1() {
        return new Product(1, "ProductA", BigDecimal.valueOf(6666.0));
    }

    /**
     * 返回对应id的产品
     * @param id 产品id
     * @return 产品
     */
    @GetMapping({"/get-product-2", "/get_product2"})
    public Product get_product2(Integer id) {
        return new Product(id, "ProductC", BigDecimal.valueOf(6666.0));
    }

    /**
     * 把给定的产品信息以 text/plain 的形式返回
     * @param product
     * @return
     */
    @GetMapping({"/get-product-3","/get_product3"})
    public String get_product3(Product product) {
        return product.toString();
    }

    /**
     * 把给定的产品以 test/plain 的形式返回
     * @param product
     * @return
     */
    @PostMapping({"/post-product-1","/post_product1"})
    public String post_product1(Product product) {
        return product.toString();
    }

    /** 接收json，转为 product，然后以 text/plain 返回
     *
     * @param product
     * @return
     */
    @PostMapping({"/post-product-2","/post_product2"})
    public String post_product2(@RequestBody Product product) {
        return product.toString();
    }

    @DeleteMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        String result = String.format("编号为%s的产品删除成功", id);
        System.out.println(result);
        return result;
    }

    @PutMapping("/update")
    public String updateByPut(Product product) {
        String result = product.toString() + " 更新成功";
        System.out.println(result);
        return result;
    }

    @PostMapping("/upload")
    public String upload(MultipartRequest request) {
        MultipartFile file = request.getFile("file");
        String originalFilename = file.getOriginalFilename();
        return "upload success filename: " + originalFilename;
    }
}
