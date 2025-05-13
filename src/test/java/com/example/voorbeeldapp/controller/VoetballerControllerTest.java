package com.example.voorbeeldapp.controller;

import com.example.voorbeeldapp.generated.model.VoetballerRequest;
import com.example.voorbeeldapp.model.VoetballerEntity;
import com.example.voorbeeldapp.repository.VoetballerRepository;
import jakarta.jms.Queue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoetballerControllerTest {

    @Mock
    private VoetballerRepository mockRepository;
    @Mock
    private JmsTemplate mockJmsTemplate;
    @Mock
    private Queue mockVoetballerQueue;
    private VoetballerController controller;

    @BeforeEach
    void setUp() {
        controller = new VoetballerController(mockRepository, mockJmsTemplate, mockVoetballerQueue);
    }

    // ✅ GET all
    @Test
    void testGetAllVoetballers() {
        VoetballerEntity v1 = new VoetballerEntity(1L, "Van Dijk", "Verdediger", "SV Hoogeveen");
        VoetballerEntity v2 = new VoetballerEntity(2L, "Memphis", "Aanvaller", "SV De Weide");

        when(mockRepository.findAll()).thenReturn(Arrays.asList(v1, v2));

        var result = controller.getVoetballers();

        assertEquals(2, result.getBody().size());
        assertEquals("Van Dijk", result.getBody().get(0).getNaam());
        assertEquals("SV De Weide", result.getBody().get(1).getTeam());
    }

    // ✅ GET by ID - found
    @Test
    void testGetVoetballerById_found() {
        VoetballerEntity v = new VoetballerEntity(1L, "Frenkie", "Middenvelder", "SV De Weide");
        when(mockRepository.findById(1L)).thenReturn(Optional.of(v));

        var result = controller.getVoetballerById(1L);

        assertEquals(200, result.getStatusCodeValue());
        assertEquals("Frenkie", result.getBody().getNaam());
    }

    // ✅ GET by ID - not found
    @Test
    void testGetVoetballerById_notFound() {
        when(mockRepository.findById(99L)).thenReturn(Optional.empty());

        var result = controller.getVoetballerById(99L);
        assertEquals(404, result.getStatusCodeValue());
    }

    // ✅ POST create
    @Test
    void testCreateVoetballer() {
        VoetballerRequest request = new VoetballerRequest().naam("Blind").positie("Verdediger").team("SV De Weide");
        VoetballerEntity entity = new VoetballerEntity(1L, "Blind", "Verdediger", "SV De Weide");

        when(mockRepository.save(any())).thenReturn(entity);

        var response = controller.createVoetballer(request);

        assertEquals(201, response.getStatusCodeValue());
        assertEquals("Blind", response.getBody().getNaam());
        assertEquals("SV de Weide", response.getBody().getTeam());
    }

    // ✅ PUT update - found
    @Test
    void testUpdateVoetballer_found() {
        VoetballerEntity existing = new VoetballerEntity(10L, "Oud", "Keeper", "SV De Weide");
        VoetballerRequest updatedRequest = new VoetballerRequest().naam("Nieuw").positie("Doelman").team("SV De Weide");

        when(mockRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(mockRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = controller.updateVoetballer(10L, updatedRequest);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Nieuw", response.getBody().getNaam());
        assertEquals("Doelman", response.getBody().getPositie());
    }

    // ✅ PUT update - not found
    @Test
    void testUpdateVoetballer_notFound() {
        VoetballerRequest request = new VoetballerRequest().naam("Onbekend").positie("Middenvelder").team("SV De Weide");

        when(mockRepository.findById(999L)).thenReturn(Optional.empty());

        var result = controller.updateVoetballer(999L, request);

        assertEquals(404, result.getStatusCodeValue());
    }

    // ✅ DELETE - found
    @Test
    void testDeleteVoetballer_found() {
        VoetballerEntity entity = new VoetballerEntity(4L, "Sneijder", "Middenvelder", "SV De Weide");
        when(mockRepository.existsById(4L)).thenReturn(true);
        doNothing().when(mockRepository).deleteById(4L);

        var result = controller.deleteVoetballer(4L);

        assertEquals(204, result.getStatusCodeValue());
        verify(mockRepository, never()).delete(entity);
        verify(mockRepository, times(1)).deleteById(4L);
    }

    // ✅ DELETE - not found
    @Test
    void testDeleteVoetballer_notFound() {
        var result = controller.deleteVoetballer(404L);

        assertEquals(404, result.getStatusCodeValue());
        verify(mockRepository, never()).delete(any());
    }

    @Test
    void testSendVoetballer_Success() {
        VoetballerEntity entity = new VoetballerEntity();
        entity.setId(3L);
        entity.setNaam("Memphis Depay");
        entity.setPositie("Aanvaller");
        entity.setTeam("SV De Weide");

        when(mockRepository.findById(3L)).thenReturn(Optional.of(entity));

        var response = controller.sendVoetballer(3L);

        assertEquals(202, response.getStatusCodeValue());

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockJmsTemplate).convertAndSend(eq(mockVoetballerQueue), messageCaptor.capture());

        String sentMessage = messageCaptor.getValue();
        assertTrue(sentMessage.contains("Memphis Depay"));
        assertTrue(sentMessage.contains("Aanvaller"));
        assertTrue(sentMessage.contains("SV De Weide"));
    }

    @Test
    void testSendVoetballer_NotFound() {
        when(mockRepository.findById(404L)).thenReturn(Optional.empty());

        var response = controller.sendVoetballer(404L);

        assertEquals(404, response.getStatusCodeValue());
        verifyNoInteractions(mockJmsTemplate);
    }
}
