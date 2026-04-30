package com.f1pulse.backend.repository;

import com.f1pulse.backend.model.SyncMeta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncMetaRepository extends JpaRepository<SyncMeta, String> {
}
