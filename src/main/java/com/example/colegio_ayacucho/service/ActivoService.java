package com.example.colegio_ayacucho.service;

import com.example.colegio_ayacucho.dto.ActivoFormDto;
import com.example.colegio_ayacucho.dto.ActivoFiltroDto;
import com.example.colegio_ayacucho.exception.CodigoDuplicadoException;
import com.example.colegio_ayacucho.exception.RecursoNoEncontradoException;
import com.example.colegio_ayacucho.exception.ReglaNegocioException;
import com.example.colegio_ayacucho.model.Activo;
import com.example.colegio_ayacucho.model.TipoActivo;
import com.example.colegio_ayacucho.model.Ubicacion;
import com.example.colegio_ayacucho.model.enums.AccionHistorial;
import com.example.colegio_ayacucho.repository.ActivoRepository;
import com.example.colegio_ayacucho.repository.TipoActivoRepository;
import com.example.colegio_ayacucho.repository.UbicacionRepository;
import com.example.colegio_ayacucho.util.CodigoActivoUtil;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Lógica de negocio de los activos: búsqueda con filtros, CRUD, validaciones y
 * registro automático en el historial.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ActivoService {

    private static final int MAX_DETALLE = 500;

    private final ActivoRepository activoRepository;
    private final TipoActivoRepository tipoActivoRepository;
    private final UbicacionRepository ubicacionRepository;
    private final HistorialService historialService;

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------
    @Transactional(readOnly = true)
    public Page<Activo> buscar(ActivoFiltroDto filtro, Pageable pageable) {
        return activoRepository.findAll(construirFiltro(filtro), pageable);
    }

    @Transactional(readOnly = true)
    public List<Activo> listarParaExportar(ActivoFiltroDto filtro) {
        return activoRepository.findAll(construirFiltro(filtro), Sort.by(Sort.Direction.ASC, "codigo"));
    }

    @Transactional(readOnly = true)
    public Activo obtener(Long id) {
        return activoRepository.findDetalleById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el activo con ID " + id + "."));
    }

    /** Sugiere el siguiente código CI-xxx según el mayor correlativo existente. */
    @Transactional(readOnly = true)
    public String sugerirCodigo() {
        return CodigoActivoUtil.siguiente(activoRepository.maxNumeroCodigo());
    }

    // ------------------------------------------------------------------
    // Comandos
    // ------------------------------------------------------------------
    public Activo crear(ActivoFormDto dto, String username) {
        validarFechas(dto);
        String codigo = normalizarCodigo(dto.getCodigo());
        if (activoRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new CodigoDuplicadoException(
                    "El código " + codigo + " ya está registrado. Ingrese un código distinto.");
        }
        TipoActivo tipo = buscarTipo(dto.getTipoId());
        Ubicacion ubicacion = buscarUbicacion(dto.getUbicacionId());

        Activo activo = new Activo();
        aplicarDatos(activo, dto, codigo, tipo, ubicacion);
        activo = activoRepository.save(activo);

        historialService.registrar(activo, username, AccionHistorial.CREACION,
                "Activo registrado: " + activo.getCodigo() + " - " + activo.getNombre());
        return activo;
    }

    public Activo actualizar(Long id, ActivoFormDto dto, String username) {
        validarFechas(dto);
        Activo activo = obtener(id);
        String codigo = normalizarCodigo(dto.getCodigo());
        if (activoRepository.existsByCodigoIgnoreCaseAndIdNot(codigo, id)) {
            throw new CodigoDuplicadoException(
                    "El código " + codigo + " ya está registrado en otro activo.");
        }
        TipoActivo tipo = buscarTipo(dto.getTipoId());
        Ubicacion ubicacion = buscarUbicacion(dto.getUbicacionId());

        // Se calcula el detalle ANTES de modificar la entidad
        String detalle = describirCambios(activo, dto, codigo, tipo, ubicacion);

        aplicarDatos(activo, dto, codigo, tipo, ubicacion);
        activo = activoRepository.save(activo);

        historialService.registrar(activo, username, AccionHistorial.EDICION,
                detalle.isEmpty() ? "Sin cambios en los datos." : recortar(detalle));
        return activo;
    }

    public void eliminar(Long id, String username) {
        Activo activo = obtener(id);
        // El historial se conserva (activo_id queda en NULL por la FK ON DELETE SET NULL)
        historialService.registrar(activo, username, AccionHistorial.ELIMINACION,
                "Activo eliminado: " + activo.getCodigo() + " - " + activo.getNombre());
        activoRepository.delete(activo);
    }

    // ------------------------------------------------------------------
    // Apoyo
    // ------------------------------------------------------------------
    private void aplicarDatos(Activo activo, ActivoFormDto dto, String codigo,
                              TipoActivo tipo, Ubicacion ubicacion) {
        activo.setCodigo(codigo);
        activo.setNombre(dto.getNombre().trim());
        activo.setTipo(tipo);
        activo.setUbicacion(ubicacion);
        activo.setMarca(limpiar(dto.getMarca()));
        activo.setModelo(limpiar(dto.getModelo()));
        activo.setNumeroSerie(limpiar(dto.getNumeroSerie()));
        activo.setResponsable(limpiar(dto.getResponsable()));
        activo.setEstado(dto.getEstado());
        activo.setCriticidad(dto.getCriticidad());
        activo.setFechaCompra(dto.getFechaCompra());
        activo.setFechaFinGarantia(dto.getFechaFinGarantia());
        activo.setObservaciones(limpiar(dto.getObservaciones()));
    }

    private void validarFechas(ActivoFormDto dto) {
        if (dto.getFechaCompra() != null && dto.getFechaFinGarantia() != null
                && dto.getFechaFinGarantia().isBefore(dto.getFechaCompra())) {
            throw new ReglaNegocioException(
                    "La fecha de fin de garantía no puede ser anterior a la fecha de compra.");
        }
    }

    private TipoActivo buscarTipo(Long tipoId) {
        return tipoActivoRepository.findById(tipoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El tipo de activo seleccionado no existe."));
    }

    private Ubicacion buscarUbicacion(Long ubicacionId) {
        return ubicacionRepository.findById(ubicacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "La ubicación seleccionada no existe."));
    }

    private String normalizarCodigo(String codigo) {
        return codigo == null ? "" : codigo.trim().toUpperCase();
    }

    private String limpiar(String valor) {
        return StringUtils.hasText(valor) ? valor.trim() : null;
    }

    private Specification<Activo> construirFiltro(ActivoFiltroDto filtro) {
        return (root, query, cb) -> {
            // Trae tipo y ubicación en la misma consulta (evita LazyInitialization)
            if (!Long.class.equals(query.getResultType()) && !long.class.equals(query.getResultType())) {
                root.fetch("tipo", JoinType.LEFT);
                root.fetch("ubicacion", JoinType.LEFT);
                query.distinct(true);
            }

            List<Predicate> condiciones = new ArrayList<>();

            if (StringUtils.hasText(filtro.getQ())) {
                String like = "%" + filtro.getQ().toLowerCase() + "%";
                condiciones.add(cb.or(
                        cb.like(cb.lower(root.get("codigo")), like),
                        cb.like(cb.lower(root.get("nombre")), like),
                        cb.like(cb.lower(root.get("marca")), like),
                        cb.like(cb.lower(root.get("numeroSerie")), like)
                ));
            }
            if (filtro.getTipoId() != null) {
                condiciones.add(cb.equal(root.get("tipo").get("id"), filtro.getTipoId()));
            }
            if (filtro.getEstado() != null) {
                condiciones.add(cb.equal(root.get("estado"), filtro.getEstado()));
            }
            if (filtro.getUbicacionId() != null) {
                condiciones.add(cb.equal(root.get("ubicacion").get("id"), filtro.getUbicacionId()));
            }

            return cb.and(condiciones.toArray(Predicate[]::new));
        };
    }

    /** Texto legible con los campos que cambiaron, para el historial. */
    private String describirCambios(Activo a, ActivoFormDto d, String codigo,
                                    TipoActivo tipo, Ubicacion ubicacion) {
        List<String> cambios = new ArrayList<>();
        comparar(cambios, "Código", a.getCodigo(), codigo);
        comparar(cambios, "Nombre", a.getNombre(), d.getNombre());
        comparar(cambios, "Tipo", a.getTipo().getNombre(), tipo.getNombre());
        comparar(cambios, "Marca", a.getMarca(), d.getMarca());
        comparar(cambios, "Modelo", a.getModelo(), d.getModelo());
        comparar(cambios, "N° de serie", a.getNumeroSerie(), d.getNumeroSerie());
        comparar(cambios, "Ubicación", a.getUbicacion().getNombre(), ubicacion.getNombre());
        comparar(cambios, "Responsable", a.getResponsable(), d.getResponsable());
        comparar(cambios, "Estado", a.getEstado(), d.getEstado());
        comparar(cambios, "Criticidad", a.getCriticidad(), d.getCriticidad());
        comparar(cambios, "Fecha de compra", a.getFechaCompra(), d.getFechaCompra());
        comparar(cambios, "Fin de garantía", a.getFechaFinGarantia(), d.getFechaFinGarantia());
        comparar(cambios, "Observaciones", a.getObservaciones(), d.getObservaciones());
        return String.join("; ", cambios);
    }

    private void comparar(List<String> cambios, String campo, Object antes, Object despues) {
        String a = antes == null ? "" : antes.toString().trim();
        String d = despues == null ? "" : despues.toString().trim();
        if (!Objects.equals(a, d)) {
            cambios.add(campo + ": '" + (a.isEmpty() ? "(vacío)" : a) + "' → '"
                    + (d.isEmpty() ? "(vacío)" : d) + "'");
        }
    }

    private String recortar(String texto) {
        return texto.length() <= MAX_DETALLE ? texto : texto.substring(0, MAX_DETALLE - 3) + "...";
    }
}
