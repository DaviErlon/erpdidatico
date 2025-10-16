package com.example.erpserver.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.br.CNPJ;
import org.hibernate.validator.constraints.br.CPF;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FornecedorDTO {

    @CPF(message = "CPF inválido")
    private String cpf;

    @CNPJ(message = "CNPJ inválido")
    private String cnpj;

    @Size(min = 10, max = 11, message = "O telefone deve ter entre 10 e 11 digitos")
    private String telefone;

    @NotBlank(message = "nome do fornecedor não pode ser nulo ou vazia")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
    private String nome;
}

