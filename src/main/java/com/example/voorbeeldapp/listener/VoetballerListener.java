package com.example.voorbeeldapp.listener;

import com.example.voorbeeldapp.config.SftpProperties;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Slf4j
@Component
@RequiredArgsConstructor
public class VoetballerListener {

    private final SftpProperties sftpProperties;

    @JmsListener(destination = "voetballer.queue")
    public void receiveMessage(Message message) {
        try {
            if (message instanceof TextMessage textMessage) {
                String content = textMessage.getText();
                log.info("Ontvangen bericht: {}", content);

                String fileName = "voetballer_" + System.currentTimeMillis() + ".txt";
                uploadFileToSftp(fileName, content);
            } else {
                log.warn("Onbekend berichttype ontvangen");
            }
        } catch (Exception e) {
            log.error("Fout bij verwerken van bericht", e);
        }
    }

    private void uploadFileToSftp(String fileName, String content) {
        Session session = null;
        ChannelSftp channelSftp = null;
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(
                    sftpProperties.getUser(),
                    sftpProperties.getHost(),
                    sftpProperties.getPort()
            );
            session.setPassword(sftpProperties.getPassword());

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            session.connect();
            log.info("Verbonden met SFTP-server");

            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            channelSftp.cd(sftpProperties.getRemoteDir());

            try (InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
                channelSftp.put(inputStream, fileName);
                log.info("Bestand {} succesvol geüpload", fileName);
            }

        } catch (Exception e) {
            log.error("Fout bij uploaden naar SFTP", e);
        } finally {
            if (channelSftp != null && channelSftp.isConnected()) {
                channelSftp.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }
}
