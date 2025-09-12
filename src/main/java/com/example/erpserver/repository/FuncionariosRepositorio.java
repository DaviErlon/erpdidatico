package com.example.erpserver.repository;

import com.example.erpserver.entities.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FuncionariosRepositorio extends JpaRepository<Funcionario, String> {

    List<Funcionario> findByAssinanteIdAndNomeStartingWithIgnoreCase(Long assinanteId, String prefixoNome);

    void deleteByAssinanteIdAndCpf(Long assinanteId, String cpf);
}

