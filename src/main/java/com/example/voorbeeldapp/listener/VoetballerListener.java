package com.example.voorbeeldapp.listener;

import com.example.voorbeeldapp.properties.SftpProperties;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class RecordListener {

    private final SftpProperties sftpProperties;

    public RecordListener(SftpProperties sftpProperties) {
        this.sftpProperties = sftpProperties;
    }

    @JmsListener(destination = "record.queue")
    public void handleRecord(Record record) throws Exception {
        String content = String.format("ID: %d\nName: %s\nValue: %s",
                record.getId(), record.getName(), record.getValue());

        Path tempFile = Files.createTempFile("record-", ".txt");
        Files.writeString(tempFile, content);

        uploadViaSftp(tempFile);
        Files.delete(tempFile);
    }

    private void uploadViaSftp(Path file) throws Exception {
        JSch jsch = new JSch();
        Session session = jsch.getSession(sftpProperties.getUser(), sftpProperties.getHost(), sftpProperties.getPort());
        session.setPassword(sftpProperties.getPassword());
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();

        ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");
        sftp.connect();
        sftp.put(file.toString(), sftpProperties.getRemoteDir() + "/" + file.getFileName());
        sftp.disconnect();
        session.disconnect();
    }
}
