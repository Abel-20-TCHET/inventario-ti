package com.example.colegio_ayacucho.repository;

import com.example.colegio_ayacucho.model.HistorialActivo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialActivoRepository extends JpaRepository<HistorialActivo, Long> {

    /** Historial de un activo, más reciente primero, con el usuario ya cargado. */
    @EntityGraph(attributePaths = {"usuario"})
    List<HistorialActivo> findByActivoIdOrderByFechaDesc(Long activoId);

    /** Últimos movimientos del sistema para el panel de control. */
    @EntityGraph(attributePaths = {"usuario"})
    List<HistorialActivo> findTop6ByOrderByFechaDesc();
}
