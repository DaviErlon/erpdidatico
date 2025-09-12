package com.example.erpserver.DTOs;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TituloDTO {

    @NotNull(message = "Pessoa não identificada")
    private Long id;

    @NotNull(message = "A lista de produtos não pode ser nula")
    @Size(min = 1, message = "O título deve ter pelo menos 1 produto")
    private List<ItemProdutoDTO> produtos;

}