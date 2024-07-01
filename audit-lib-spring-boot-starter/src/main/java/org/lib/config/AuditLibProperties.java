package org.lib.config;

import jakarta.annotation.PostConstruct;
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

    private String filePath;

    @PostConstruct
    private void init() {
        System.out.println("props init");
    }

}
