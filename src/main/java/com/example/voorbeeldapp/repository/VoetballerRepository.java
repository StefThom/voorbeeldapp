package com.example.voorbeeldapp.repository;

import com.example.voorbeeldapp.model.VoetballerEntity;
import org.springframework.data.repository.CrudRepository;

public interface VoetballerRepository extends CrudRepository<VoetballerEntity, Long> {
}
