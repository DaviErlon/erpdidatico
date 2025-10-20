package com.example.erpserver.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "titulos",
        indexes = {
                @Index(name = "idx_titulo_ceo", columnList = "ceo_id"),
                @Index(name = "idx_titulo_cliente", columnList = "cliente_id"),
                @Index(name = "idx_titulo_fornecedor", columnList = "fornecedor_id"),
                @Index(name = "idx_titulo_funcionario", columnList = "funcionario_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Titulo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false)
    private boolean pago = false;

    @Column(nullable = false)
    private boolean recebidoNoEstoque = false;

    @Column(length = 11)
    private String cpf;

    @Column(length = 14)
    private String cnpj;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "funcionario_id")
    private Funcionario funcionario;

    @OneToMany(mappedBy = "titulo",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private Set<ProdutosDosTitulos> produtosDosTitulos = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        if (this.criadoEm == null) {
            this.criadoEm = OffsetDateTime.now();
        }

        if (this.valor != null && this.valor.signum() == 0) {
            this.valor = BigDecimal.ZERO;
        }
    }
}
