package com.example.colegio_ayacucho.dto;

import com.example.colegio_ayacucho.model.Activo;
import com.example.colegio_ayacucho.model.HistorialActivo;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Datos agregados para el panel de control.
 *
 * Incluye, además de los contadores, las listas que alimentan las tarjetas de
 * alerta y la actividad reciente del sistema.
 */
@Getter
@Builder
public class DashboardDto {

    private final long total;
    private final long operativos;
    private final long enReparacion;
    private final long deBaja;

    private final long garantiasPorVencer;
    private final long garantiasVencidas;
    private final long garantiasVigentes;
    private final long garantiasSinRegistro;

    private final List<ConteoPorTipoDto> porTipo;
    private final List<Activo> alertasPorVencer;
    private final List<Activo> alertasVencidas;
    private final List<HistorialActivo> movimientos;

    // ------------------------------------------------------------------
    // Porcentajes calculados para las barras de las tarjetas KPI
    // ------------------------------------------------------------------
    public long getPorcentajeOperativos() {
        return porcentaje(operativos);
    }

    public long getPorcentajeEnReparacion() {
        return porcentaje(enReparacion);
    }

    public long getPorcentajeDeBaja() {
        return porcentaje(deBaja);
    }

    public long getPorcentajeGarantiasPorVencer() {
        return porcentaje(garantiasPorVencer);
    }

    public long getPorcentajeGarantiasVencidas() {
        return porcentaje(garantiasVencidas);
    }

    public long getPorcentajeGarantiasVigentes() {
        return porcentaje(garantiasVigentes);
    }

    public long getPorcentajeGarantiasSinRegistro() {
        return porcentaje(garantiasSinRegistro);
    }

    private long porcentaje(long parte) {
        return total <= 0 ? 0 : Math.round(parte * 100.0 / total);
    }
}
