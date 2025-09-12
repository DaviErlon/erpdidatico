package com.example.erpserver.repository;

import com.example.erpserver.entities.Titulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TitulosRepositorio extends JpaRepository<Titulo, Long>, JpaSpecificationExecutor<Titulo> {

    // Buscar título específico pelo ID do assinante
    Optional<Titulo> findByAssinanteIdAndId(Long assinanteId, Long tituloId);
}
