package com.example.colegio_ayacucho.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UbicacionFormDto {

    private Long id;

    @NotBlank(message = "El nombre de la ubicación es obligatorio.")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres.")
    private String nombre;
}
