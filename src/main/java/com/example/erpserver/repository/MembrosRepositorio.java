package com.example.erpserver.repository;

import com.example.erpserver.entities.Membro;
import com.example.erpserver.entities.Pessoa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MembrosRepositorio extends JpaRepository<Membro, Long> {

    Page<Membro> findByAssinanteIdAndNomeStartingWithIgnoreCase(Long assinanteId, String prefixoNome, Pageable pageable);

    Optional<Membro> findByEmail(String email);

    int countByAssinanteId(Long assinanteId);
}
