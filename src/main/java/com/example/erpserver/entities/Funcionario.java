package com.example.erpserver.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "funcionarios",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"email"}),
                @UniqueConstraint(columnNames = {"token_autorizacao"})
        },
        indexes = {
                @Index(name = "idx_funcionario_setor", columnList = "setor"),
                @Index(name = "idx_funcionario_especializacao", columnList = "especializacao"),
                @Index(name = "idx_funcionario_assinante", columnList = "assinante_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Funcionario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(nullable = false, length = 11)
    private String cpf;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal salario;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal bonus;

    @Column(nullable = false)
    private String setor;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assinante_id", nullable = false)
    private Ceo ceo;

    @Column(unique = true)
    private String email;

    @Column(name = "senha_hash")
    @JsonIgnore
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private TipoEspecializacao especializacao;

    @Column(name = "token_autorizacao", unique = true, length = 6)
    private String tokenAutorizacao;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @PrePersist
    protected void onCreate() {
        if (this.criadoEm == null) {
            this.criadoEm = OffsetDateTime.now();
        }

        if (TipoEspecializacao.GESTOR.equals(this.especializacao) && this.tokenAutorizacao == null) {
            this.tokenAutorizacao = gerarTokenAleatorio(6);
        }
    }

    @PrePersist
    @PreUpdate
    private void validarEspecializacaoComLogin() {
        if (especializacao != null && (email == null || senhaHash == null)) {
            throw new IllegalStateException(
                    "Funcionário com especialização deve possuir email e senha para login"
            );
        }
        if (especializacao == null && (email != null || senhaHash != null)) {
            throw new IllegalStateException(
                    "Funcionário sem especialização não pode ter login"
            );
        }
        if (TipoEspecializacao.GESTOR.equals(especializacao) && tokenAutorizacao == null) {
            throw new IllegalStateException(
                    "Funcionário gerente deve possuir token de autorização"
            );
        }
    }

    private static String gerarTokenAleatorio(int tamanho) {
        final String caracteres = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(tamanho);
        for (int i = 0; i < tamanho; i++) {
            int index = random.nextInt(caracteres.length());
            sb.append(caracteres.charAt(index));
        }
        return sb.toString();
    }
}
