package com.example.erpserver.repository;

import com.example.erpserver.entities.ProdutosDosTitulos;
import com.example.erpserver.entities.Titulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutosDosTitulosRepositorio extends JpaRepository<ProdutosDosTitulos, Long> {

    List<ProdutosDosTitulos> findByTituloId(Long tituloId);

    void deleteByTitulo(Titulo titulo);
}
