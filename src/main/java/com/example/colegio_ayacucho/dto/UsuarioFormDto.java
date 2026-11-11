package com.example.colegio_ayacucho.dto;

import com.example.colegio_ayacucho.model.enums.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Formulario de usuarios. La contraseña es obligatoria al crear y opcional al
 * editar (si se deja vacía se conserva la actual).
 */
@Getter
@Setter
public class UsuarioFormDto {

    private Long id;

    @NotBlank(message = "El nombre de usuario es obligatorio.")
    @Size(min = 4, max = 50, message = "El usuario debe tener entre 4 y 50 caracteres.")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$",
             message = "El usuario solo admite letras, números, punto, guion y guion bajo.")
    private String username;

    /** Vacío en edición = no cambiar la contraseña. */
    @Size(max = 60, message = "La contraseña no puede superar los 60 caracteres.")
    private String password;

    @NotBlank(message = "El nombre completo es obligatorio.")
    @Size(max = 120, message = "El nombre completo no puede superar los 120 caracteres.")
    private String nombreCompleto;

    @NotNull(message = "Debe seleccionar un rol.")
    private Rol rol;

    private boolean activo = true;
}
