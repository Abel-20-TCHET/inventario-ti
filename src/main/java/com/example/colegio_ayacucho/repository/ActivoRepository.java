package com.example.colegio_ayacucho.repository;

import com.example.colegio_ayacucho.dto.ConteoPorTipoDto;
import com.example.colegio_ayacucho.model.Activo;
import com.example.colegio_ayacucho.model.enums.EstadoActivo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ActivoRepository extends JpaRepository<Activo, Long>, JpaSpecificationExecutor<Activo> {

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);

    boolean existsByTipoId(Long tipoId);

    boolean existsByUbicacionId(Long ubicacionId);

    // ------------------- Dashboard -------------------
    long countByEstado(EstadoActivo estado);

    long countByFechaFinGarantiaBefore(LocalDate fecha);

    long countByFechaFinGarantiaBetween(LocalDate desde, LocalDate hasta);

    long countByFechaFinGarantiaIsNull();

    /** Equipos cuya garantía vence pronto (para la tarjeta de alertas). */
    @EntityGraph(attributePaths = {"tipo", "ubicacion"})
    List<Activo> findTop5ByFechaFinGarantiaBetweenOrderByFechaFinGarantiaAsc(
            LocalDate desde, LocalDate hasta);

    /** Equipos con garantía ya vencida (para la tarjeta de alertas). */
    @EntityGraph(attributePaths = {"tipo", "ubicacion"})
    List<Activo> findTop5ByFechaFinGarantiaBeforeOrderByFechaFinGarantiaDesc(LocalDate fecha);

    @Query("""
            select new com.example.colegio_ayacucho.dto.ConteoPorTipoDto(a.tipo.nombre, count(a))
              from Activo a
             group by a.tipo.nombre
             order by count(a) desc
            """)
    List<ConteoPorTipoDto> contarPorTipo();

    // ------------------- Detalle -------------------
    @EntityGraph(attributePaths = {"tipo", "ubicacion"})
    @Query("select a from Activo a where a.id = :id")
    Optional<Activo> findDetalleById(@Param("id") Long id);

    /** Mayor correlativo numérico de los códigos CI-xxx (para sugerir el siguiente). */
    @Query(value = "select max(cast(substring(codigo from 4) as integer)) from activos", nativeQuery = true)
    Integer maxNumeroCodigo();
}
