package com.example.colegio_ayacucho.dto;

import com.example.colegio_ayacucho.model.enums.EstadoActivo;
import org.springframework.util.StringUtils;

/**
 * Parámetros del buscador y los filtros del listado de activos.
 * Se enlaza automáticamente desde los query params (?q=...&tipoId=...).
 */
public class ActivoFiltroDto {

    private String q;
    private Long tipoId;
    private EstadoActivo estado;
    private Long ubicacionId;
    private int page;

    public String getQ() {
        return q == null ? null : q.trim();
    }

    public void setQ(String q) {
        this.q = q;
    }

    public Long getTipoId() {
        return tipoId;
    }

    public void setTipoId(Long tipoId) {
        this.tipoId = tipoId;
    }

    public EstadoActivo getEstado() {
        return estado;
    }

    public void setEstado(EstadoActivo estado) {
        this.estado = estado;
    }

    public Long getUbicacionId() {
        return ubicacionId;
    }

    public void setUbicacionId(Long ubicacionId) {
        this.ubicacionId = ubicacionId;
    }

    public int getPage() {
        return Math.max(page, 0);
    }

    public void setPage(int page) {
        this.page = page;
    }

    /** true cuando no hay ningún criterio aplicado. */
    public boolean isVacio() {
        return !StringUtils.hasText(q) && tipoId == null && estado == null && ubicacionId == null;
    }
}
