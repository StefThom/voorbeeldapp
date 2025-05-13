package com.example.voorbeeldapp.controller;

import com.example.voorbeeldapp.generated.api.VoetballersApi;
import com.example.voorbeeldapp.generated.model.Voetballer;
import com.example.voorbeeldapp.generated.model.VoetballerRequest;
import com.example.voorbeeldapp.model.VoetballerEntity;
import com.example.voorbeeldapp.repository.VoetballerRepository;
import jakarta.jms.Queue;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class VoetballerController implements VoetballersApi {

    private final VoetballerRepository repository;
    private final JmsTemplate jmsTemplate;
    private final Queue voetballerQueue;

    // Helper: Entity naar API-model
    private Voetballer toModel(VoetballerEntity entity) {
        return new Voetballer()
                .id(entity.getId())
                .naam(entity.getNaam())
                .positie(entity.getPositie())
                .team(entity.getTeam());
    }

    @Override
    public ResponseEntity<List<Voetballer>> getVoetballers() {
        List<VoetballerEntity> voetballers = (List<VoetballerEntity>) repository.findAll();
        List<Voetballer> result = voetballers.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<Voetballer> createVoetballer(VoetballerRequest voetballerRequest) {
        VoetballerEntity entity = new VoetballerEntity();
        entity.setNaam(voetballerRequest.getNaam());
        entity.setPositie(voetballerRequest.getPositie());
        entity.setTeam(voetballerRequest.getTeam());

        VoetballerEntity saved = repository.save(entity);
        return ResponseEntity.status(201).body(toModel(saved));
    }

    @Override
    public ResponseEntity<Voetballer> getVoetballerById(Long id) {
        Optional<VoetballerEntity> opt = repository.findById(id);
        return opt.map(entity -> ResponseEntity.ok(toModel(entity)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<Voetballer> updateVoetballer(Long id, VoetballerRequest request) {
        return repository.findById(id).map(entity -> {
            entity.setNaam(request.getNaam());
            entity.setPositie(request.getPositie());
            entity.setTeam(request.getTeam());
            VoetballerEntity updated = repository.save(entity);
            return ResponseEntity.ok(toModel(updated));
        }).orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<Void> deleteVoetballer(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @Override
    public ResponseEntity<Void> sendVoetballer(Long id) {
        Optional<VoetballerEntity> opt = repository.findById(id);
        if (opt.isPresent()) {
            VoetballerEntity entity = opt.get();
            // Maak een eenvoudige String van het bericht, bv. JSON of simpel tekstformaat
            String message = String.format("Voetballer: %s (%s, %s)", entity.getNaam(), entity.getPositie(), entity.getTeam());

            // Stuur naar ActiveMQ
            jmsTemplate.convertAndSend(voetballerQueue, message);

            return ResponseEntity.accepted().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
