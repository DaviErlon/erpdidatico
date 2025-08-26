package com.example.erpserver.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Produto implements ID{
    private String id;
    private String nome;
    private double preco;
    private int estoque;

}
