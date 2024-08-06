package com.onedlvb.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 * Class for handling properties for annotation
 * @author Matushkin Anton
 */
@Data
@ConfigurationProperties(prefix = "audit-lib-spring-boot-starter")
public class AuditLibProperties {

    private boolean consoleEnabled;

    private boolean fileEnabled;

    private boolean kafkaLogEnabled;

    private String filePath;

}
