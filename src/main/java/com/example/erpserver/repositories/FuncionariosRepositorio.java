package com.example.erpserver.repositories;

import com.example.erpserver.entities.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FuncionariosRepositorio extends JpaRepository<Funcionario, UUID>, JpaSpecificationExecutor<Funcionario> {

    List<Funcionario> findAllByCeoId(UUID ceoId);

    Optional<Funcionario> findByEmail(String email);

    Optional<Funcionario> findByCeoIdAndId(UUID ceoId, UUID funcionarioId);

    Optional<Funcionario> findByCeoIdAndCpf(UUID ceoId, String cpf);

    boolean existsByCeoIdAndCpf(UUID ceoId, String cpf);

    long countByCeoId(UUID ceoId);

    boolean existsByEmailOrCpf(String email, String cpf);

    Optional<Funcionario> findByCeoIdAndTokenAutorizacao(UUID ceoId, String tokenSeguranca);
}

