package com.example.colegio_ayacucho.dto;

import com.example.colegio_ayacucho.model.enums.Criticidad;
import com.example.colegio_ayacucho.model.enums.EstadoActivo;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Formulario de alta/edición de activos. Es un DTO: la entidad Activo no se
 * expone directamente a la vista.
 */
@Getter
@Setter
public class ActivoFormDto {

    private Long id;

    @NotBlank(message = "El código es obligatorio.")
    @Size(max = 10, message = "El código no puede superar los 10 caracteres.")
    @Pattern(regexp = "^CI-[0-9]{3,}$", message = "El código debe tener el formato CI-001.")
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 150, message = "El nombre no puede superar los 150 caracteres.")
    private String nombre;

    @NotNull(message = "Debe seleccionar un tipo de activo.")
    private Long tipoId;

    @Size(max = 80, message = "La marca no puede superar los 80 caracteres.")
    private String marca;

    @Size(max = 80, message = "El modelo no puede superar los 80 caracteres.")
    private String modelo;

    @Size(max = 100, message = "El número de serie no puede superar los 100 caracteres.")
    private String numeroSerie;

    @NotNull(message = "Debe seleccionar una ubicación.")
    private Long ubicacionId;

    @Size(max = 120, message = "El responsable no puede superar los 120 caracteres.")
    private String responsable;

    @NotNull(message = "Debe seleccionar un estado.")
    private EstadoActivo estado = EstadoActivo.OPERATIVO;

    @NotNull(message = "Debe seleccionar la criticidad.")
    private Criticidad criticidad = Criticidad.MEDIA;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaCompra;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaFinGarantia;

    @Size(max = 500, message = "Las observaciones no pueden superar los 500 caracteres.")
    private String observaciones;

    /**
     * Regla de negocio: la garantía no puede terminar antes de la compra.
     * El mensaje se muestra en el bloque de errores del formulario.
     */
    @AssertTrue(message = "La fecha de fin de garantía no puede ser anterior a la fecha de compra.")
    public boolean isFechasCoherentes() {
        if (fechaCompra == null || fechaFinGarantia == null) {
            return true;
        }
        return !fechaFinGarantia.isBefore(fechaCompra);
    }
}
