package com.lib.auditlib.service.impl;

import com.lib.auditlib.service.ProductService;
import org.lib.advice.AuditLog;
import org.lib.advice.LogLevel;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {

    @Override
    @AuditLog(logLevel = LogLevel.INFO)
    public String getProduct(String product) {
        return product;
    }

    @Override
    @AuditLog
    public Double calculateAvg(Integer a, Integer b) {
        return (double) a / b;
    }

}
