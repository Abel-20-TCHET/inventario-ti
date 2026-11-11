package com.example.colegio_ayacucho.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CambioPasswordDto {

    @NotBlank(message = "Debe ingresar su contraseña actual.")
    private String actual;

    @NotBlank(message = "Debe ingresar la nueva contraseña.")
    @Size(min = 8, max = 60, message = "La nueva contraseña debe tener al menos 8 caracteres.")
    private String nueva;

    @NotBlank(message = "Debe confirmar la nueva contraseña.")
    private String confirmar;
}
