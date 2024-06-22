package com.lib.auditlib.service.impl;

import com.lib.auditlib.service.ProductService;
import org.lib.advice.AuditLog;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {

    @Override
    @AuditLog
    public String getProduct() {
        return "Product";
    }

}
