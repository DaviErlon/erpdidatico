package com.example.erpserver.repository;

import com.example.erpserver.entities.Assinante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssinantesRepositorio extends JpaRepository<Assinante, Long> {
    Optional<Assinante> findByEmail(String email);

    Optional<Assinante> findByCpf(String cpf);
}
