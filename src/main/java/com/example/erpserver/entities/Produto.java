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
        name = "produtos",
        indexes = {
                @Index(name = "idx_produto_nome", columnList = "nome"),
                @Index(name = "idx_produto_ceo", columnList = "ceo_id"),
                @Index(name = "idx_produto_ceo", columnList = "ceo_id"),
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal preco;

    @Column(nullable = false)
    private long estoqueDisponivel;

    @Column(nullable = false)
    private long estoquePendente;

    @Column(nullable = false)
    private long estoqueReservado;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assinante_id", nullable = false)
    private Ceo ceo;

    @JsonIgnore
    @OneToMany(mappedBy = "produto",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private Set<ProdutosDosTitulos> produtosDosTitulos = new HashSet<>();

    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @PrePersist
    protected void onCreate() {
        if (this.criadoEm == null) {
            this.criadoEm = OffsetDateTime.now();
        }
    }
}
