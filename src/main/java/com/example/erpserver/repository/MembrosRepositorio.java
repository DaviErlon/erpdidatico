package com.example.erpserver.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MembrosRepositorio extends JpaRepository<Gerente, Long> {

    Page<Gerente> findByAssinanteIdAndNomeStartingWithIgnoreCase(Long assinanteId, String prefixoNome, Pageable pageable);

    Optional<Gerente> findByEmail(String email);

    int countByAssinanteId(Long assinanteId);
}
