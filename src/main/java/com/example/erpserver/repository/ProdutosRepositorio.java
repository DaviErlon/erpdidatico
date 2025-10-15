package com.example.erpserver.repository;

import com.example.erpserver.entities.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProdutosRepositorio extends JpaRepository<Produto, UUID>, JpaSpecificationExecutor<Produto> {

    Optional<Produto> findByAssinanteIdAndId(UUID assinanteId, UUID produtoId);
}



