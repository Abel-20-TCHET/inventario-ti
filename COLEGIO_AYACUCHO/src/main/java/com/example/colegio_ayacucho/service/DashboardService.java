package com.example.colegio_ayacucho.service;

import com.example.colegio_ayacucho.dto.DashboardDto;
import com.example.colegio_ayacucho.model.enums.EstadoActivo;
import com.example.colegio_ayacucho.repository.ActivoRepository;
import com.example.colegio_ayacucho.repository.HistorialActivoRepository;
import com.example.colegio_ayacucho.util.GarantiaUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Cálculo de los indicadores del panel de control.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    /** Máximo de equipos que se listan en las tarjetas de alerta. */
    private static final int LIMITE_ALERTAS = 5;
    /** Máximo de movimientos recientes mostrados. */
    private static final int LIMITE_MOVIMIENTOS = 6;

    private final ActivoRepository activoRepository;
    private final HistorialActivoRepository historialRepository;

    public DashboardDto obtenerResumen() {
        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(GarantiaUtil.DIAS_ALERTA);

        long total = activoRepository.count();
        long porVencer = activoRepository.countByFechaFinGarantiaBetween(hoy, limite);
        long vencidas = activoRepository.countByFechaFinGarantiaBefore(hoy);
        long sinRegistro = activoRepository.countByFechaFinGarantiaIsNull();
        // Vigentes = todo lo que tiene fecha y todavía no entra en ninguna alerta
        long vigentes = Math.max(0, total - porVencer - vencidas - sinRegistro);

        return DashboardDto.builder()
                .total(total)
                .operativos(activoRepository.countByEstado(EstadoActivo.OPERATIVO))
                .enReparacion(activoRepository.countByEstado(EstadoActivo.EN_REPARACION))
                .deBaja(activoRepository.countByEstado(EstadoActivo.DE_BAJA))
                .garantiasPorVencer(porVencer)
                .garantiasVencidas(vencidas)
                .garantiasVigentes(vigentes)
                .garantiasSinRegistro(sinRegistro)
                .porTipo(activoRepository.contarPorTipo())
                .alertasPorVencer(activoRepository
                        .findTop5ByFechaFinGarantiaBetweenOrderByFechaFinGarantiaAsc(hoy, limite))
                .alertasVencidas(activoRepository
                        .findTop5ByFechaFinGarantiaBeforeOrderByFechaFinGarantiaDesc(hoy))
                .movimientos(historialRepository
                        .findTop6ByOrderByFechaDesc())
                .build();
    }
}
