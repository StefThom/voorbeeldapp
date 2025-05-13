package com.example.voorbeeldapp.service;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.AcceptAllPasswordAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EmbeddedSftpServiceTest {

    private static SshServer embeddedSftpServer;
    private static final int SFTP_PORT = 2222;
    private static final String USERNAME = "test";
    private static final String PASSWORD = "test";
    private static final Path SFTP_ROOT_DIR = Paths.get("target/sftp-root");

    private Session session;
    private ChannelSftp sftpChannel;

    @BeforeAll
    static void startSftpServer() throws IOException {
        embeddedSftpServer = SshServer.setUpDefaultServer();
        embeddedSftpServer.setPort(SFTP_PORT);
        embeddedSftpServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
        embeddedSftpServer.setPasswordAuthenticator(AcceptAllPasswordAuthenticator.INSTANCE);
        embeddedSftpServer.setSubsystemFactories(Collections.singletonList(new SftpSubsystemFactory()));
        Files.createDirectories(SFTP_ROOT_DIR);
        embeddedSftpServer.setFileSystemFactory(new VirtualFileSystemFactory(SFTP_ROOT_DIR.toAbsolutePath()));
        embeddedSftpServer.start();
    }

    @AfterAll
    static void stopSftpServer() throws IOException {
        if (embeddedSftpServer != null) {
            embeddedSftpServer.stop();
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        JSch jsch = new JSch();
        session = jsch.getSession(USERNAME, "localhost", SFTP_PORT);
        session.setPassword(PASSWORD);
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();

        var channel = session.openChannel("sftp");
        channel.connect();
        sftpChannel = (ChannelSftp) channel;
    }

    @AfterEach
    void tearDown() {
        if (sftpChannel != null && sftpChannel.isConnected()) {
            sftpChannel.disconnect();
        }
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }

    @Test
    void testUploadFileToEmbeddedSftpServer() throws Exception {
        String remoteDir = "/";
        String fileName = "test-bestand.txt";
        String content = "Hallo, SFTP!";

        sftpChannel.cd(remoteDir);
        sftpChannel.put(new ByteArrayInputStream(content.getBytes()), fileName);

        Path uploadedFile = SFTP_ROOT_DIR.resolve(fileName);
        assertTrue(Files.exists(uploadedFile), "Bestand moet op SFTP-server staan");
    }
}
