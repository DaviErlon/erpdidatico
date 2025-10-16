package com.example.erpserver.DTOs;

import com.example.erpserver.entities.TipoEspecializacao;
import com.example.erpserver.entities.TipoPlano;
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

    private TipoPlano plano;

    private TipoEspecializacao tipo;

    private boolean assinante;

    private String nome;
}
