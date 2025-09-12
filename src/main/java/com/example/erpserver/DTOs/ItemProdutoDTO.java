package com.example.erpserver.DTOs;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ItemProdutoDTO {

    @NotNull(message = "ID do produto é obrigatório")
    private Long produtoId;

    @Min(value = 1, message = "Quantidade deve ser pelo menos 1")
    private int quantidade;
}