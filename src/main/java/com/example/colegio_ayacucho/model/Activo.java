package com.example.colegio_ayacucho.model;

import com.example.colegio_ayacucho.model.enums.Criticidad;
import com.example.colegio_ayacucho.model.enums.EstadoActivo;
import com.example.colegio_ayacucho.util.EstadoGarantia;
import com.example.colegio_ayacucho.util.GarantiaUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Activo de TI: unidad central de la CMDB.
 */
@Entity
@Table(name = "activos",
       uniqueConstraints = @UniqueConstraint(name = "uq_activos_codigo", columnNames = "codigo"))
@Getter
@Setter
@NoArgsConstructor
public class Activo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Código institucional único, formato CI-001. */
    @Column(nullable = false, unique = true, length = 10)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nombre;

    @ManyToOne(optional = false, fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "tipo_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_activos_tipo"))
    private TipoActivo tipo;

    @Column(length = 80)
    private String marca;

    @Column(length = 80)
    private String modelo;

    @Column(name = "numero_serie", length = 100)
    private String numeroSerie;

    @ManyToOne(optional = false, fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "ubicacion_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_activos_ubicacion"))
    private Ubicacion ubicacion;

    @Column(length = 120)
    private String responsable;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoActivo estado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Criticidad criticidad = Criticidad.MEDIA;

    @Column(name = "fecha_compra")
    private LocalDate fechaCompra;

    @Column(name = "fecha_fin_garantia")
    private LocalDate fechaFinGarantia;

    @Column(length = 500)
    private String observaciones;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // ------------------------------------------------------------------
    // Ciclo de vida: fechas de auditoría automáticas
    // ------------------------------------------------------------------
    @PrePersist
    void alCrear() {
        this.fechaRegistro = LocalDateTime.now();
    }

    @PreUpdate
    void alActualizar() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    // ------------------------------------------------------------------
    // Propiedades calculadas (no persistidas) usadas por Thymeleaf
    // ------------------------------------------------------------------
    @Transient
    public EstadoGarantia getEstadoGarantia() {
        return GarantiaUtil.estado(fechaFinGarantia);
    }

    @Transient
    public String getGarantiaTexto() {
        return GarantiaUtil.texto(fechaFinGarantia);
    }
}
