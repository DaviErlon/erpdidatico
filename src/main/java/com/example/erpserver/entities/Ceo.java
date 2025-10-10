package com.example.erpserver.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "assinantes",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"cpf"}),
                @UniqueConstraint(columnNames = {"email"})
        },
        indexes = {
                @Index(name = "idx_assinante_cpf", columnList = "cpf"),
                @Index(name = "idx_assinante_email", columnList = "email")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Ceo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "token_autorizacao", nullable = false, length = 6, unique = true, updatable = false)
    private String tokenAutorizacao;

    @Column(nullable = false, unique = true, length = 11)
    @ToString.Exclude
    private String cpf;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "senha_hash", nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ToString.Exclude
    private String senhaHash;

    private int plano;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @JsonIgnore
    @OneToMany(mappedBy = "assinante",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<Funcionario> funcionarios = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "assinante",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<Clientes> clientes = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "assinante",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<Clientes> fornecedores = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "assinante",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<Produto> produtos = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "assinante",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<Titulo> titulos = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        if (this.criadoEm == null) {
            this.criadoEm = OffsetDateTime.now();
        }

        if (this.tokenAutorizacao == null) {
            this.tokenAutorizacao = gerarTokenAleatorio(6);
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
