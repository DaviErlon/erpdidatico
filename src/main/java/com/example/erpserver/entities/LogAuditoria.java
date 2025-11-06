package com.example.erpserver.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "logsAuditoria",
        indexes = {
                @Index(name = "idx_log_funcionario", columnList = "funcionario_id"),
                @Index(name = "idx_log_ceo", columnList = "ceo_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LogAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ceo_id", nullable = false)
    private Ceo ceo;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "funcionario_id", nullable = false)
    private Funcionario funcionario;  // funcionario que emitiu

    @Column(nullable = false)
    private String nome;

    private String telefone;

    @Column(nullable = false, length = 11)
    private String cpf;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String setor = "Setor não declarado";

    private String acao;           // ex: "CRIAR_PRODUTO", "ATUALIZAR_FUNCIONARIO"
    private String entidade;       // ex: "Produto", "Funcionario", "Titulo"
    private UUID entidadeId;       // ex: ID do produto/título
    private String detalhes;       // JSON, descrição, etc.

    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @PrePersist
    protected void onCreate() {
        if (this.criadoEm == null) {
            this.criadoEm = OffsetDateTime.now();
        }
    }
}

