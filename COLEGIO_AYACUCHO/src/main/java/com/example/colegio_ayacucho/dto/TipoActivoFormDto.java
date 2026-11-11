package com.example.colegio_ayacucho.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TipoActivoFormDto {

    private Long id;

    @NotBlank(message = "El nombre del tipo es obligatorio.")
    @Size(max = 60, message = "El nombre no puede superar los 60 caracteres.")
    private String nombre;
}
