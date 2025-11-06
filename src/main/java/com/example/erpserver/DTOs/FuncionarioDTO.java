package com.example.erpserver.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.br.CPF;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FuncionarioDTO {

    @NotBlank(message = "O CPF é obrigatório")
    @CPF(message = "CPF inválido")
    private String cpf;

    @Positive(message = "O salário deve ser positivo")
    private BigDecimal salario;

    @Positive(message = "O bônus deve ser positivo")
    private BigDecimal bonus;

    @Size(min = 10, max = 11, message = "O telefone deve ter entre 10 e 11 digitos")
    private String telefone;

    @NotBlank(message = "nome de pessoa não pode ser nulo ou vazia")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
    private String nome;

    @Size(min = 3, max = 100, message = "O nome do setor deve ter entre 3 e 100 caracteres")
    private String setor;
}
