package com.example.erpserver.repository;

import com.example.erpserver.entities.Ceo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CeoRepositorio extends JpaRepository<Ceo, UUID> {
    Optional<Ceo> findByEmail(String email);

    Optional<Ceo> findByCpf(String cpf);
}
