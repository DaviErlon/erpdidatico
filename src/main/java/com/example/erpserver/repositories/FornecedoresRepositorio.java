package com.example.erpserver.repositories;

import com.example.erpserver.entities.Fornecedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FornecedoresRepositorio extends JpaRepository<Fornecedor, UUID>, JpaSpecificationExecutor<Fornecedor> {

    Optional<Fornecedor> findByCeoIdAndId(UUID ceoId, UUID id);

    Optional<Fornecedor> findByCeoIdAndCpf(UUID ceoId, String cpf);

    Optional<Fornecedor> findByCeoIdAndCnpj(UUID ceoId, String cnpj);

    boolean existsByCeoIdAndCpf(UUID ceoId, String cpf);

    boolean existsByCeoIdAndCnpj(UUID ceoId, String cnpj);
}
