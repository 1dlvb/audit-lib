# Audit-lib

Audit-lib is a Java library that provides an annotation-based logging mechanism. It allows developers to easily log essential information about method executions, including the method name, arguments, return value, and any thrown exceptions.

## Features

- **Method Information Logging**: Automatically logs the method name, arguments, return value, and exceptions.
- **HTTP Request and Response Logging**: Automatically logs details of HTTP requests and responses for annotated methods. 
- **Flexible Configuration**: Configure logging output through `application.properties` with options for console and file outputs.
- **Configurable Log Levels**: Specify the log level for logging using the `@AuditLogHttp` or `@AuditLog` annotation.
- **Easy Integration**: Seamlessly integrate with Spring applications using a simple annotation-based setup.

## Getting Started

### Configuration
Configure the logging preferences in your application.properties file:

+ Enable or disable console logging
`audit-lib-spring-boot-starter.file-enabled=true`
+ Enable or disable file logging
`audit-lib-spring-boot-starter.console-enabled=true`
+ Specify the file path for log output
`audit-lib-spring-boot-starter.file-path=path`

### Usage
To use Audit-lib, simply annotate your methods with `@AuditLog` or `@AuditLogHttp`. Here's an example:
```java
import com.example.auditlib.annotation.AuditLog;

public class TransactionService {

    @AuditLog
    public void processTransaction(Transaction transaction) {
        // method implementation
    }
}
```
```java
import com.onedlvb.advice.annotation.AuditLogHttp;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {

    @AuditLogHttp
    @PostMapping("/processPayment")
    public PaymentResponse processPayment(PaymentRequest request) {
        // method implementation
    }
}

```
### Advanced Usage
Setting the Log Level
The `@AuditLog` and `@AuditLogHttp` annotation allows you to specify the log level for each method. To set the log level, pass the LogLevel enum to the annotation like so:
```java
import com.example.auditlib.annotation.AuditLog;
import com.example.auditlib.annotation.LogLevel;

public class PaymentService {

    @AuditLog(LogLevel = LogLevel.INFO)
    public void initiatePayment(PaymentDetails details) {
        // method implementation
    }
}
```
```java
import com.onedlvb.advice.annotation.AuditLogHttp;
import com.onedlvb.advice.LogLevel;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {

    @AuditLogHttp(logLevel = LogLevel.INFO)
    @PostMapping("/processPayment")
    public PaymentResponse processPayment(PaymentRequest request) {
        // method implementation
    }
}

```
This will log the method execution details at the INFO level. You can choose from various log levels such as DEBUG, INFO, WARN, ERROR, etc., depending on your logging strategy.

