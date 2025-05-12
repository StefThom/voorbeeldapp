package com.example.voorbeeldapp.listener;

import com.example.voorbeeldapp.service.SftpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class VoetballerListenerTest {

    private SftpService sftpService;
    private VoetballerListener listener;

    @BeforeEach
    void setUp() {
        sftpService = mock(SftpService.class);
        listener = new VoetballerListener(sftpService);
    }

    @Test
    void testOnMessage_callsSftpUpload() {
        String message = "Voetballer: Frenkie de Jong, Positie: Middenvelder";
        listener.onMessage(message);
        verify(sftpService, times(1)).uploadVoetballerToSftp(message);
    }
}
