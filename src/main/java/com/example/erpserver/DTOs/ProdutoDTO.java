package com.example.erpserver.DTOs;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProdutoDTO {

    @NotBlank(message = "nome de produto não pode ser nulo ou vazia")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
    private String nome;

    @DecimalMin(value = "0.00", inclusive = false, message = "O valor não pode ser negativo ou zero")
    @DecimalMax(value = "100000000.00", inclusive = true, message = "O valor não pode ser maior que 100 milhões")
    private BigDecimal preco;

    @Min(value = 0, message = "A quantidade em estoque não pode ser negativa")
    @Max(value = 100000000, message = "A quantidade não pode ser maior que 100 milhões")
    private long quantidade;

}
