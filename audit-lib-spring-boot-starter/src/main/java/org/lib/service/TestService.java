package org.lib.service;

import org.lib.advice.AuditLog;
import org.lib.advice.LogLevel;
import org.springframework.stereotype.Service;

@Service
public class TestService {

    @AuditLog(logLevel = LogLevel.INFO)
    public String performAction(String action) {
        return action;
    }

    @AuditLog
    public Double calculateAvg(Integer a, Integer b) {
        if (b == 0) {
            throw new IllegalArgumentException("Possible zero division!");
        }
        return (double) a / b;
    }

    @AuditLog(logLevel = LogLevel.INFO)
    public void printHello() {
        System.out.println("Hello");
    }

    @AuditLog(logLevel = LogLevel.WARN)
    public void throwIllegalArgumentException() {
        throw new IllegalArgumentException();
    }

}
