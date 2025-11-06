package com.example.erpserver.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "clientes",
        indexes = {
                @Index(name = "idx_cliente_nome", columnList = "nome"),
                @Index(name = "idx_cliente_cpf", columnList = "cpf"),
                @Index(name = "idx_cliente_ceo", columnList = "ceo_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 11)
    private String cpf;

    @Column(nullable = false)
    private String nome;

    @Column(length = 11)
    private String telefone;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ceo_id", nullable = false)
    private Ceo ceo;

    @PrePersist
    protected void onCreate() {
        if (this.criadoEm == null) {
            this.criadoEm = OffsetDateTime.now();
        }
    }
}
