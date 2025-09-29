package com.example.erpserver.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RespostaDTO {
    private String token;
    private int plano;
    private boolean assinante;
    private String nome;
}
