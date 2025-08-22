package com.erp.models;


/**
 * Conversão para classe record, além de moderno, é próprio para
 * classes que representam dados
 *
 * @param tipo 1 - Cliente, 2 - Fornecedor, 3 - Funcionario
 */
public record Pessoa(String id, int tipo, String nome) {

    @Override
    public String toString() {
        return id + "," + tipo + "," + nome;
    }

    public static Pessoa fromString(String str) {
        String[] parts = str.split(",");
        return new Pessoa(parts[0], Integer.parseInt(parts[1]), parts[2]);
    }
}
