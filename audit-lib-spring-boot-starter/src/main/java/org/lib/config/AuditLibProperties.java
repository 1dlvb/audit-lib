package org.lib.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Class for handling properties for annotation
 * @author Matushkin Anton
 */
@ConfigurationProperties(prefix = "audit-lib-spring-boot-starter")
public class AuditLibProperties {
}
