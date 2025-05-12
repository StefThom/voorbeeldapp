package com.example.voorbeeldapp.service;

import com.example.voorbeeldapp.config.SftpProperties;
import com.jcraft.jsch.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SftpServiceTest {

    @Mock
    private SftpProperties mockSftpProperties;
    @Mock
    private JSch mockJsch;
    @Mock
    private Session mockSession;
    @Mock
    private ChannelSftp mockChannelSftp;
    @Captor
    private ArgumentCaptor<String> fileNameCaptor;

    private SftpService sftpServiceUnderTest;

    @BeforeEach
    void setUp() throws Exception {
        when(mockSftpProperties.getHost()).thenReturn("localhost");
        when(mockSftpProperties.getPort()).thenReturn(22);
        when(mockSftpProperties.getUser()).thenReturn("sftpuser");
        when(mockSftpProperties.getPassword()).thenReturn("sftppassword");

        sftpServiceUnderTest = new SftpService(mockSftpProperties) {
            @Override
            protected JSch createJsch() {
                return mockJsch;
            }

            @Override
            protected ChannelSftp getSftpChannel(Session session) {
                return mockChannelSftp;
            }
        };

        when(mockJsch.getSession(anyString(), anyString(), anyInt())).thenReturn(mockSession);
    }

    @Test
    void testUploadVoetballerToSftp_success() throws Exception {
        when(mockSftpProperties.getRemoteDir()).thenReturn("/upload/voetballers/");
        String content = "Voetballer: Bergwijn, Positie: Aanvaller";

        sftpServiceUnderTest.uploadVoetballerToSftp(content);

        verify(mockChannelSftp, times(1)).connect();
        verify(mockChannelSftp).cd("/upload/voetballers/");
        verify(mockChannelSftp).put((InputStream) any(), fileNameCaptor.capture());
    }
    @Test
    void testSessionConnectionFailure() throws Exception {
        doThrow(new JSchException("connectie mislukt")).when(mockSession).connect();

        assertThrows(RuntimeException.class, () ->
                sftpServiceUnderTest.uploadVoetballerToSftp("Test speler")
        );

        verify(mockSession).connect();
        verify(mockSession, never()).disconnect();  // verbinding is nooit gelukt
    }

    @Test
    void testChannelConnectionFailure() throws Exception {
        when(mockChannelSftp.isConnected()).thenReturn(false);
        when(mockSession.isConnected()).thenReturn(true);
        doThrow(new JSchException("kanaal connectie mislukt")).when(mockChannelSftp).connect();

        assertThrows(RuntimeException.class, () ->
                sftpServiceUnderTest.uploadVoetballerToSftp("Test speler")
        );

        verify(mockChannelSftp).connect();
        verify(mockSession).disconnect();  // moet alsnog netjes sluiten
    }

    @Test
    void testPutFailure() throws Exception {
        when(mockChannelSftp.isConnected()).thenReturn(true);
        when(mockSession.isConnected()).thenReturn(true);
        doThrow(new SftpException(4, "upload mislukt")).when(mockChannelSftp).put((InputStream) any(), fileNameCaptor.capture());

        assertThrows(RuntimeException.class, () ->
                sftpServiceUnderTest.uploadVoetballerToSftp("Test speler")
        );

        verify(mockChannelSftp).put((InputStream) any(), fileNameCaptor.capture());
        verify(mockChannelSftp).disconnect();
        verify(mockSession).disconnect();
    }

    @Test
    void testChannelCreationFailure() throws Exception {
        when(mockSession.isConnected()).thenReturn(true);
        sftpServiceUnderTest = new SftpService(mockSftpProperties) {
            @Override
            protected JSch createJsch() {
                return mockJsch;
            }

            @Override
            protected ChannelSftp getSftpChannel(Session session) throws JSchException {
                throw new JSchException("kanaal fout");
            }
        };

        assertThrows(RuntimeException.class, () ->
                sftpServiceUnderTest.uploadVoetballerToSftp("Test speler")
        );

        verify(mockSession).connect();
        verify(mockSession).disconnect();
    }

}
