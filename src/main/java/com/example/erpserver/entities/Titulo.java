package com.example.erpserver.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "titulos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Titulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double valor;

    private boolean pago = false;

    private LocalDateTime criadoEm = LocalDateTime.now();

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "assinante_id", nullable = false)
    private Assinante assinante;

    @ManyToOne
    @JoinColumn(name = "pessoa_id", referencedColumnName = "id", nullable = false)
    private Pessoa pessoa;

    @OneToMany(mappedBy = "titulo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProdutosDosTitulos> produtosDosTitulos;
}
