package com.example.erpserver.repository;

import com.example.erpserver.entities.Titulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TitulosRepositorio extends JpaRepository<Titulo, UUID>, JpaSpecificationExecutor<Titulo> {

    Optional<Titulo> findByCeoIdAndId(UUID assinanteId, UUID tituloId);
}
