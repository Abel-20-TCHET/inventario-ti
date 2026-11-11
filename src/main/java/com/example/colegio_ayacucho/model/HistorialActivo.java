package com.example.colegio_ayacucho.model;

import com.example.colegio_ayacucho.model.enums.AccionHistorial;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Registro de auditoría: quién hizo qué y cuándo sobre un activo.
 *
 * El vínculo con el activo es opcional a propósito: cuando se elimina un
 * activo, su historial se conserva con activo_id = NULL para no perder la
 * trazabilidad de la baja (el detalle guarda el código y nombre del equipo).
 */
@Entity
@Table(name = "historial_activo")
@Getter
@Setter
@NoArgsConstructor
public class HistorialActivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activo_id",
                foreignKey = @ForeignKey(name = "fk_historial_activo"))
    private Activo activo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id",
                foreignKey = @ForeignKey(name = "fk_historial_usuario"))
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccionHistorial accion;

    @Column(length = 500)
    private String detalle;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @PrePersist
    void alCrear() {
        if (this.fecha == null) {
            this.fecha = LocalDateTime.now();
        }
    }

    /** Nombre del usuario que ejecutó la acción (o "Sistema"). */
    public String getNombreUsuario() {
        return usuario != null ? usuario.getNombreCompleto() : "Sistema";
    }
}
