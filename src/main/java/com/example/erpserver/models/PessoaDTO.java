package com.example.erpserver.models;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.br.CPF;

@Getter
@Setter
@AllArgsConstructor
public class PessoaDTO {

    @NotBlank(message = "O CPF é obrigatório")
    @CPF(message = "CPF inválido")
    private String id;

    @Min(value = 0, message = "Tipo de pessoa não reconhecida")
    @Max(value = 2, message = "Tipo de pessoa não reconhecida")
    private int tipo;

    @NotBlank(message = "nome de pessoa não pode ser nulo ou vazia")
    private String nome;

    @Override
    public String toString() {
        String tipoNome = switch (tipo) {
            case 0 -> "Cliente";
            case 1 -> "Funcionario";
            case 2 -> "Fornecedor";
            default -> "error";
        };
        return "\n\nCPF: " + this.id + "\nNome: "+ this.nome +"\ntipo: " + tipoNome;
    }
}
