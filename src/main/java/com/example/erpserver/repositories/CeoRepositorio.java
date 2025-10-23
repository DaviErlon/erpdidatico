package com.example.erpserver.repositories;

import com.example.erpserver.entities.Ceo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CeoRepositorio extends JpaRepository<Ceo, UUID> {

    Optional<Ceo> findByEmail(String email);

    boolean existsByEmailOrCpf(String email, String cpf);
}
