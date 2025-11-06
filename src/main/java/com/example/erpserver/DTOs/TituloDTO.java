package com.example.erpserver.DTOs;

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

    private List<ItemProdutoDTO> produtos;
}