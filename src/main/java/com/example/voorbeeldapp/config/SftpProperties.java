package com.example.voorbeeldapp.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sftp")
public class SftpProperties {
    private String host;
    private int port;
    private String user;
    private String password;
    private String remoteDir;
    // getters and setters
}
