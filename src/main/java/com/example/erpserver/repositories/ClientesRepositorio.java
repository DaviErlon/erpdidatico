package com.example.erpserver.repositories;

import com.example.erpserver.entities.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientesRepositorio extends JpaRepository<Cliente, UUID>, JpaSpecificationExecutor<Cliente> {

    Optional<Cliente> findByCeoIdAndId(UUID ceoId, UUID id);

    Optional<Cliente> findByCeoIdAndCpf(UUID ceoId, String cpf);

    boolean existsByCeoIdAndCpf(UUID ceoId, String cpf);
}

