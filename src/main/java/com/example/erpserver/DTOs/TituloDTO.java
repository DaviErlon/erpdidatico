package com.example.erpserver.DTOs;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TituloDTO {

    // está associado ou não a um cliente ou fornecedor
    private UUID id;

    private boolean pagarOuReceber;

    @NotNull(message = "A lista de produtos não pode ser nula")
    @Size(min = 1, message = "O título deve ter pelo menos 1 produto")
    private List<ItemProdutoDTO> produtos;
}