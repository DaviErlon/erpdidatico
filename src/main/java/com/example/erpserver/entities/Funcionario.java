package com.example.erpserver.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
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
                @Index(name = "idx_funcionario_tipo", columnList = "tipo"),
                @Index(name = "idx_funcionario_ceo", columnList = "ceo_id"),
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Funcionario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 11)
    private String cpf;

    @Column(nullable = false)
    private String nome;

    private String telefone;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal salario;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal bonus;

    @Column(nullable = false)
    private String setor;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ceo_id", nullable = false)
    private Ceo ceo;

    @Column(unique = true)
    private String email;

    @Column(name = "senha_hash")
    @JsonIgnore
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private TipoEspecializacao tipo;

    @Column(name = "token_autorizacao", unique = true, length = 6)
    private String tokenAutorizacao;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @JsonIgnore
    @OneToMany(mappedBy = "funcionario",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private Set<LogAuditoria> logAuditorias = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        if (this.criadoEm == null) {
            this.criadoEm = OffsetDateTime.now();
        }
    }

    @PreUpdate
    private void validarEspecializacaoComLogin() {
        if (TipoEspecializacao.GESTOR.equals(this.tipo) && this.tokenAutorizacao == null) {
            this.tokenAutorizacao = gerarTokenAleatorio(6);
        }
        if(!TipoEspecializacao.GESTOR.equals(this.tipo) && this.tokenAutorizacao != null){
            this.tokenAutorizacao = null;
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
