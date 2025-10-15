package com.example.erpserver.repository;

import com.example.erpserver.entities.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FuncionariosRepositorio extends JpaRepository<Funcionario, UUID> {

    void deleteByAssinanteIdAndCpf(UUID assinanteId, String cpf);
}

