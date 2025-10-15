package com.example.erpserver.repository;

import com.example.erpserver.entities.Clientes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PessoasRepositorio extends JpaRepository<Clientes, UUID>, JpaSpecificationExecutor<Clientes> {

    Optional<Clientes> findByAssinanteIdAndId(UUID assinanteId, UUID id);

    void deleteByAssinanteIdAndId(UUID assinanteId, UUID id);
}


