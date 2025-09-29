package com.example.erpserver.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.br.CPF;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PessoaDTO {

    @NotBlank(message = "O CPF é obrigatório")
    @CPF(message = "CPF inválido")
    private String cpf;

    @NotBlank(message = "contato não pode ser nulo")
    private String contato;

    private boolean fornecedor;

    @NotBlank(message = "nome de pessoa não pode ser nulo ou vazia")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
    private String nome;
}
