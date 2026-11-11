package com.example.colegio_ayacucho.service;

import com.example.colegio_ayacucho.dto.TipoActivoFormDto;
import com.example.colegio_ayacucho.exception.RecursoNoEncontradoException;
import com.example.colegio_ayacucho.exception.ReglaNegocioException;
import com.example.colegio_ayacucho.model.TipoActivo;
import com.example.colegio_ayacucho.repository.ActivoRepository;
import com.example.colegio_ayacucho.repository.TipoActivoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TipoActivoService {

    private final TipoActivoRepository tipoActivoRepository;
    private final ActivoRepository activoRepository;

    @Transactional(readOnly = true)
    public List<TipoActivo> listar() {
        return tipoActivoRepository.findAllByOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public TipoActivo obtener(Long id) {
        return tipoActivoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el tipo de activo con ID " + id + "."));
    }

    public TipoActivo crear(TipoActivoFormDto dto) {
        String nombre = dto.getNombre().trim();
        if (tipoActivoRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ReglaNegocioException("Ya existe un tipo de activo con el nombre '" + nombre + "'.");
        }
        return tipoActivoRepository.save(TipoActivo.builder().nombre(nombre).build());
    }

    public TipoActivo actualizar(Long id, TipoActivoFormDto dto) {
        TipoActivo tipo = obtener(id);
        String nombre = dto.getNombre().trim();
        if (tipoActivoRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new ReglaNegocioException("Ya existe otro tipo de activo con el nombre '" + nombre + "'.");
        }
        tipo.setNombre(nombre);
        return tipoActivoRepository.save(tipo);
    }

    public void eliminar(Long id) {
        TipoActivo tipo = obtener(id);
        if (activoRepository.existsByTipoId(id)) {
            throw new ReglaNegocioException(
                    "No se puede eliminar el tipo '" + tipo.getNombre() + "' porque tiene activos asociados.");
        }
        tipoActivoRepository.delete(tipo);
    }
}
