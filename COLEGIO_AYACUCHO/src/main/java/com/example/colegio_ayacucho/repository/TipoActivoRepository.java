package com.example.colegio_ayacucho.repository;

import com.example.colegio_ayacucho.model.TipoActivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TipoActivoRepository extends JpaRepository<TipoActivo, Long> {

    List<TipoActivo> findAllByOrderByNombreAsc();

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
