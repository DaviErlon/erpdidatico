package com.example.erpserver.repositories;

import com.example.erpserver.entities.LogAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface LogAuditoriaRepository extends JpaRepository<LogAuditoria, UUID>, JpaSpecificationExecutor<LogAuditoria> {

}
