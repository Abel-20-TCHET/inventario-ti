package com.example.colegio_ayacucho.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Fila de la tabla "equipos por tipo" del dashboard.
 */
@Getter
@AllArgsConstructor
public class ConteoPorTipoDto {

    private final String tipo;
    private final Long total;
}
