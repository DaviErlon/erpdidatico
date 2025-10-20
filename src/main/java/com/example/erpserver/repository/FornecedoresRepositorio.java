package com.example.erpserver.repository;

import com.example.erpserver.entities.Fornecedor;
import org.hibernate.validator.constraints.br.CPF;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FornecedoresRepositorio extends JpaRepository<Fornecedor, UUID>, JpaSpecificationExecutor<Fornecedor> {

    Optional<Fornecedor> findByCeoIdAndCpf(UUID ceoId, String cpf);

    Optional<Fornecedor> findByCeoIdAndId(UUID ceoId, UUID id);

}
