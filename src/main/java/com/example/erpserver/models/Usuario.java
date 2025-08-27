package com.example.erpserver.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Usuario {

    private String login;
    private String senha;

    @Override
    public String toString() {
        return "\n\nUsuário: " + login;
    }
}
