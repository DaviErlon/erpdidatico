package com.erp.models;

/*
 * Conversão para classe record, além de moderno, é próprio para
 * classes que representam dados
 */
public record Produto(String id, String nome, double preco) {

    @Override
    public String toString() {
        return id + "," + nome + "," + preco;
    }

    public static Produto fromString(String str) {
        String[] parts = str.split(",");
        return new Produto(parts[0], parts[1], Double.parseDouble(parts[2]));
    }
}
