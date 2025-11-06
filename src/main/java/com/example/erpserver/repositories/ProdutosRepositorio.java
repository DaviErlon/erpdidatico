package com.example.erpserver.repositories;

import com.example.erpserver.entities.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProdutosRepositorio extends JpaRepository<Produto, UUID>, JpaSpecificationExecutor<Produto> {

    Optional<Produto> findByCeoIdAndId(UUID ceoId, UUID produtoId);

    List<Produto> findAllByCeoIdAndIdIn(UUID ceoId, List<UUID> produtoIds);
}



