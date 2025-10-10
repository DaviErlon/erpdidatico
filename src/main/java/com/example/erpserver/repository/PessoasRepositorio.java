package com.example.erpserver.repository;

import com.example.erpserver.entities.Clientes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PessoasRepositorio extends JpaRepository<Clientes, Long>, JpaSpecificationExecutor<Clientes> {

    Optional<Clientes> findByAssinanteIdAndId(Long assinanteId, Long id);

    void deleteByAssinanteIdAndId(Long assinanteId, Long id);
}


