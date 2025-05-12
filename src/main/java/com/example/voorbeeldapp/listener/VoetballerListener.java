package com.example.voorbeeldapp.listener;

import com.example.voorbeeldapp.service.SftpService;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class VoetballerListener {

    private final SftpService sftpService;

    public VoetballerListener(SftpService sftpService) {
        this.sftpService = sftpService;
    }

    @JmsListener(destination = "voetballerQueue")
    public void onMessage(String message) {
        sftpService.uploadVoetballerToSftp(message);
    }
}
