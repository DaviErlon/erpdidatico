package com.example.erpserver.repository;

import com.example.erpserver.entities.Pessoa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PessoasRepositorio extends JpaRepository<Pessoa, Long>, JpaSpecificationExecutor<Pessoa> {

    Optional<Pessoa> findByAssinanteIdAndId(Long assinanteId, Long id);

    void deleteByAssinanteIdAndId(Long assinanteId, Long id);
}


