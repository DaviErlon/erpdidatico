package com.example.erpserver.repository;

import com.example.erpserver.entities.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FuncionariosRepositorio extends JpaRepository<Funcionario, UUID>, JpaSpecificationExecutor<Funcionario> {

    Optional<Funcionario> findByEmail(String email);

    long countByCeoId(UUID ceoId);

    Optional<Funcionario> findByCeoIdAndId(UUID ceoId, UUID funcionarioId);
}

