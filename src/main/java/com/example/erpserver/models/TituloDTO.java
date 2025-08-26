package com.example.erpserver.models;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.br.CPF;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TituloDTO {

    @Min(value = 0, message = "O valor não pode ser negativo")
    @Max(value = 1_000_000, message = "O valor não pode ser maior que 1 milhão")
    private double valor;

    @NotBlank(message = "O CPF é obrigatório")
    @CPF(message = "CPF inválido")
    private String cpf;

    private boolean pagaroureceber;

    @Override
    public String toString() {
        String pagaroureceberString;
        if(this.pagaroureceber){
            pagaroureceberString = "A Pagar";
        } else {
            pagaroureceberString = "A Receber";
        }
        return "\n\nValor: " + valor + "\nCPF: " + cpf + "\n" + pagaroureceberString;
    }
}