package com.example.colegio_ayacucho.service;

import com.example.colegio_ayacucho.dto.UbicacionFormDto;
import com.example.colegio_ayacucho.exception.RecursoNoEncontradoException;
import com.example.colegio_ayacucho.exception.ReglaNegocioException;
import com.example.colegio_ayacucho.model.Ubicacion;
import com.example.colegio_ayacucho.repository.ActivoRepository;
import com.example.colegio_ayacucho.repository.UbicacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UbicacionService {

    private final UbicacionRepository ubicacionRepository;
    private final ActivoRepository activoRepository;

    @Transactional(readOnly = true)
    public List<Ubicacion> listar() {
        return ubicacionRepository.findAllByOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public Ubicacion obtener(Long id) {
        return ubicacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la ubicación con ID " + id + "."));
    }

    public Ubicacion crear(UbicacionFormDto dto) {
        String nombre = dto.getNombre().trim();
        if (ubicacionRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ReglaNegocioException("Ya existe una ubicación con el nombre '" + nombre + "'.");
        }
        return ubicacionRepository.save(Ubicacion.builder().nombre(nombre).build());
    }

    public Ubicacion actualizar(Long id, UbicacionFormDto dto) {
        Ubicacion ubicacion = obtener(id);
        String nombre = dto.getNombre().trim();
        if (ubicacionRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new ReglaNegocioException("Ya existe otra ubicación con el nombre '" + nombre + "'.");
        }
        ubicacion.setNombre(nombre);
        return ubicacionRepository.save(ubicacion);
    }

    public void eliminar(Long id) {
        Ubicacion ubicacion = obtener(id);
        if (activoRepository.existsByUbicacionId(id)) {
            throw new ReglaNegocioException(
                    "No se puede eliminar la ubicación '" + ubicacion.getNombre()
                            + "' porque tiene activos asociados.");
        }
        ubicacionRepository.delete(ubicacion);
    }
}
