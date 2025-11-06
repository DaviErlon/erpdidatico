package com.example.erpserver.DTOs;

import com.example.erpserver.entities.TipoEspecializacao;
import com.example.erpserver.entities.TipoPlano;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RespostaDTO {

    private String token;

    private TipoPlano plano;

    private TipoEspecializacao tipo;

    private String nome;
}