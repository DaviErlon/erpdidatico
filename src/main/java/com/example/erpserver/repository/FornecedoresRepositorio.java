package com.example.erpserver.repository;

import com.example.erpserver.entities.Fornecedor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FornecedoresRepositorio extends JpaRepository<Fornecedor, UUID>, JpaSpecificationExecutor<Fornecedor> {

    Page<Fornecedor> findByAssinanteIdAndNomeStartingWithIgnoreCase(UUID assinanteId, String prefixoNome, Pageable pageable);

    Optional<Fornecedor> findByEmail(String email);

    int countByAssinanteId(UUID assinanteId);
}
