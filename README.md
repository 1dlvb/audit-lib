# Audit-lib

Audit-lib is a Java library that provides an annotation-based logging mechanism. It allows developers to easily log essential information about method executions, including the method name, arguments, return value, and any thrown exceptions.

## Features

- **Method Information Logging**: Automatically logs the method name, arguments, return value, and exceptions.
- **Flexible Configuration**: Configure logging output through `application.properties` with options for console and file outputs.
- **Easy Integration**: Simple annotation-based setup to integrate with existing Java applications.

## Getting Started

### Configuration
Configure the logging preferences in your application.properties file:

+ Enable or disable console logging
`auditlib.console.enabled=true`

+ Enable or disable file logging
`auditlib.file.enabled=true`

+ Specify the file path for log output
`auditlib.file.path=/path/to/logfile.log`
### Usage
To use Audit-lib, simply annotate your methods with `@AuditLog`. Here's an example:
```java
import com.example.auditlib.annotation.AuditLog;

public class TransactionService {

    @AuditLog
    public void processTransaction(Transaction transaction) {
        // method implementation
    }
}
```
### Advanced Usage
Setting the Log Level
The `@AuditLog` annotation allows you to specify the log level for each method. To set the log level, pass the LogLevel enum to the annotation like so:
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
This will log the method execution details at the INFO level. You can choose from various log levels such as DEBUG, INFO, WARN, ERROR, etc., depending on your logging strategy.

