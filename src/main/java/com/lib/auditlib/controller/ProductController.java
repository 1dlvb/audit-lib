package com.lib.auditlib.controller;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import com.lib.auditlib.service.ProductService;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableAutoConfiguration
@RequiredArgsConstructor
@RequestMapping("api/audit-log/v1/product")
public class ProductController {

    @NonNull
    private final ProductService productService;

    @GetMapping
    public String getProduct() {
        return productService.getProduct();
    }

}
