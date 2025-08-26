package com.example.erpserver.models;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class ProdutoDTO {

    @NotBlank(message = "nome de produto não pode ser nulo ou vazia")
    private String nome;

    @DecimalMin(value = "0.00", inclusive = false, message = "O valor não pode ser negativo ou zero")
    @DecimalMax(value = "1000000.00", inclusive = true, message = "O valor não pode ser maior que 1 milhão")
    private double preco;

    @Min(value = 0, message = "A quantidade em estoque não pode ser negativa")
    @DecimalMax(value = "1000000.00", inclusive = true, message = "O valor não pode ser maior que 1 milhão")
    private int estoque;

    @Override
    public String toString() {
        return "\n\nNome: " + nome + "\nPreço: " + this.preco + "\nEstoque: " + this.estoque;
    }
}
