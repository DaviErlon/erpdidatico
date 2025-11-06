package com.example.erpserver.DTOs;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaginaDTO<T> {

    private List<T> dados;

    private int pagina;

    private int tamanho;

    @JsonIgnore
    public static <T> PaginaDTO<T> from(Page<T> page) {
        return new PaginaDTO<>(
                page.getContent(),
                page.getNumber(),
                page.getSize()
        );
    }
}