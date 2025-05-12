package com.example.voorbeeldapp.service;

import com.example.voorbeeldapp.config.SftpProperties;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Service
@RequiredArgsConstructor
public class SftpService {

    private final SftpProperties sftpProperties;

    public void uploadVoetballerToSftp(String content) {
        Session session = null;
        ChannelSftp channelSftp = null;

        try {
            JSch jsch = createJsch();
            session = jsch.getSession(sftpProperties.getUser(), sftpProperties.getHost(), sftpProperties.getPort());
            session.setPassword(sftpProperties.getPassword());

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();

            channelSftp = getSftpChannel(session);
            channelSftp.connect();
            channelSftp.cd(sftpProperties.getRemoteDir());

            byte[] data = content.getBytes(StandardCharsets.UTF_8);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            String fileName = "voetballer_" + System.currentTimeMillis() + ".txt";
            channelSftp.put(inputStream, fileName);

        } catch (Exception e) {
            throw new RuntimeException("SFTP upload failed", e);
        } finally {
            if (channelSftp != null && channelSftp.isConnected()) channelSftp.disconnect();
            if (session != null && session.isConnected()) session.disconnect();
        }
    }

    protected JSch createJsch() {
        return new JSch();
    }

    protected ChannelSftp getSftpChannel(Session session) throws Exception {
        return (ChannelSftp) session.openChannel("sftp");
    }
}
