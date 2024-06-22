package org.lib.config;

import org.lib.advice.AuditLog;
import org.lib.advice.AuditLogAdvice;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@ConditionalOnClass({AuditLog.class})
@EnableAspectJAutoProxy
@EnableConfigurationProperties(AuditLibProperties.class)
public class AuditLibSpringBootStarterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AuditLogAdvice auditLogAdvice() {
        return new AuditLogAdvice();
    }

}
