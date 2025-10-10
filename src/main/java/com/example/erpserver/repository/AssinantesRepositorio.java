package com.example.erpserver.repository;

import com.example.erpserver.entities.Ceo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssinantesRepositorio extends JpaRepository<Ceo, Long> {
    Optional<Ceo> findByEmail(String email);

    Optional<Ceo> findByCpf(String cpf);
}
