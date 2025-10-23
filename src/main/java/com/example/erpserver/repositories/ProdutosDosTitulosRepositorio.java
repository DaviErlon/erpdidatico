package com.example.erpserver.repositories;

import com.example.erpserver.entities.ProdutosDosTitulos;
import com.example.erpserver.entities.Titulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProdutosDosTitulosRepositorio extends JpaRepository<ProdutosDosTitulos, UUID> {

    List<ProdutosDosTitulos> findByTituloId(UUID tituloId);

}
