package com.example.voorbeeldapp.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.AcceptAllPasswordAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

@Component
@ConfigurationProperties(prefix = "sftp")
@Getter
@Setter
public class SftpConfig {
    private SshServer embeddedSftpServer;

    private String host;
    private int port;
    private String user;
    private String password;
    private String remoteDir;
//
//    @Bean
//    public SshServer startSftpServer() throws IOException {
//        embeddedSftpServer = SshServer.setUpDefaultServer();
//        embeddedSftpServer.setPort(getPort());
//        embeddedSftpServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
//        embeddedSftpServer.setPasswordAuthenticator(AcceptAllPasswordAuthenticator.INSTANCE);
//        embeddedSftpServer.setSubsystemFactories(Collections.singletonList(new SftpSubsystemFactory()));
//        Files.createDirectories(Path.of(getRemoteDir()));
//        embeddedSftpServer.setFileSystemFactory(new VirtualFileSystemFactory(Paths.get(getRemoteDir())));
//        embeddedSftpServer.start();
//        return embeddedSftpServer;
//    }
}
