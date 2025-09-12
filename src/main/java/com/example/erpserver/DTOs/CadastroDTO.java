package com.example.erpserver.DTOs;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.br.CPF;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CadastroDTO {

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, max = 50, message = "A senha deve ter entre 8 e 50 caracteres")
    private String senha;

    @NotBlank(message = "CPF é obrigatório")
    @CPF
    private String cpf;

    @Min(value = 0, message = "Tipo inválido")
    @Max(value = 2, message = "Tipo inválido")
    private int plano;
}