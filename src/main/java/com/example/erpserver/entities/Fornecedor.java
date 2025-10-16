package com.example.erpserver.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "fornecedores",
        indexes = {
                @Index(name = "idx_fornecedor_cpf", columnList = "cpf"),
                @Index(name = "idx_fornecedor_cnpj", columnList = "cnpj"),
                @Index(name = "idx_fornecedor_nome", columnList = "nome")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fornecedor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 11)
    private String cpf;

    @Column(length = 14)
    private String cnpj;

    @Column(nullable = false)
    private String nome;

    private String telefone;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assinante_id", nullable = false)
    private Ceo ceo;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @PrePersist
    protected void onCreate() {
        if (this.criadoEm == null) {
            this.criadoEm = OffsetDateTime.now();
        }

        if ((cpf == null || cpf.isBlank()) && (cnpj == null || cnpj.isBlank())) {
            throw new IllegalStateException("Fornecedor deve possuir CPF ou CNPJ");
        }
    }
}
