package com.example.colegio_ayacucho.service;

import com.example.colegio_ayacucho.model.Activo;
import com.example.colegio_ayacucho.model.HistorialActivo;
import com.example.colegio_ayacucho.model.Usuario;
import com.example.colegio_ayacucho.model.enums.AccionHistorial;
import com.example.colegio_ayacucho.repository.HistorialActivoRepository;
import com.example.colegio_ayacucho.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Auditoría de cambios sobre activos.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class HistorialService {

    private final HistorialActivoRepository historialRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Registra un movimiento. Se usa saveAndFlush para que, en el caso de una
     * eliminación, la fila del historial exista antes del DELETE del activo.
     */
    public void registrar(Activo activo, String username, AccionHistorial accion, String detalle) {
        Usuario usuario = username == null ? null
                : usuarioRepository.findByUsername(username).orElse(null);

        HistorialActivo h = new HistorialActivo();
        h.setActivo(activo);
        h.setUsuario(usuario);
        h.setAccion(accion);
        h.setDetalle(detalle);
        historialRepository.saveAndFlush(h);
    }

    @Transactional(readOnly = true)
    public List<HistorialActivo> listarPorActivo(Long activoId) {
        return historialRepository.findByActivoIdOrderByFechaDesc(activoId);
    }
}
