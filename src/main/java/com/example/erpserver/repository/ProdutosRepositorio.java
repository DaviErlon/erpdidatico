package com.example.erpserver.repository;

import com.example.erpserver.entities.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProdutosRepositorio extends JpaRepository<Produto, Long>, JpaSpecificationExecutor<Produto> {

    // Buscar produto específico por assinante e ID do produto
    Optional<Produto> findByAssinanteIdAndId(Long assinanteId, Long produtoId);
}



