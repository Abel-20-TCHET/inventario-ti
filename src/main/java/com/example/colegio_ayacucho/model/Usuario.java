package com.example.colegio_ayacucho.model;

import com.example.colegio_ayacucho.model.enums.Rol;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Usuario del sistema. La contraseña SIEMPRE se almacena cifrada con BCrypt.
 */
@Entity
@Table(name = "usuarios",
       uniqueConstraints = @UniqueConstraint(name = "uq_usuarios_username", columnNames = "username"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /** Hash BCrypt de 60 caracteres. Nunca se guarda la contraseña en claro. */
    @Column(nullable = false, length = 100)
    private String password;

    @Column(name = "nombre_completo", nullable = false, length = 120)
    private String nombreCompleto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;

    /**
     * Marca las cuentas creadas con contraseña temporal: el sistema obliga a
     * cambiarla en el primer ingreso.
     */
    @Column(name = "debe_cambiar_password", nullable = false)
    @Builder.Default
    private boolean debeCambiarPassword = true;

    /** Getter auxiliar para las vistas. */
    public String getNombreRol() {
        return rol != null ? rol.getEtiqueta() : "";
    }
}
