package com.example.erpserver.repositories;

import com.example.erpserver.entities.Titulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TitulosRepositorio extends JpaRepository<Titulo, UUID>, JpaSpecificationExecutor<Titulo> {

    Optional<Titulo> findByCeoIdAndId(UUID assinanteId, UUID tituloId);

    Optional<Titulo> findByCeoIdAndIdAndRecebidoNoEstoqueFalseAndFuncionarioIsNullAndAprovadoTrueAndPagoTrueAndValorLessThan(UUID ceoId, UUID id, BigDecimal valor);

    Optional<Titulo> findByCeoIdAndIdAndPagoFalseAndAprovadoTrue(UUID ceoId, UUID tituloId);

}
