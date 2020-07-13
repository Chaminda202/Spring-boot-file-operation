package com.spring.fileopertion.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
@Data
public class ApplicationProperties {
    private String allowedExtensions;
    private String uploadDir;
    private int fileCount;
}