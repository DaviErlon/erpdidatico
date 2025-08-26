package com.example.erpserver.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Titulo implements ID{
    private String id;
    private double valor;
    private String cpf;
    private boolean pago;
    private boolean pagaroureceber;

}

