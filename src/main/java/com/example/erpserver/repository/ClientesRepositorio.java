package com.example.erpserver.repository;

import com.example.erpserver.entities.Clientes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientesRepositorio extends JpaRepository<Clientes, UUID>, JpaSpecificationExecutor<Clientes> {

    Page<Clientes> findByAssinanteIdAndNomeStartingWithIgnoreCase(UUID assinanteId, String prefixoNome, Pageable pageable);

    Optional<Clientes> findByEmail(String email);

    int countByAssinanteId(UUID assinanteId);
}

