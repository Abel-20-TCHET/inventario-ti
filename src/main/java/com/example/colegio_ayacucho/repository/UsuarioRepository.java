package com.example.colegio_ayacucho.repository;

import com.example.colegio_ayacucho.model.Usuario;
import com.example.colegio_ayacucho.model.enums.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCaseAndIdNot(String username, Long id);

    List<Usuario> findAllByOrderByNombreCompletoAsc();

    long countByRolAndActivoTrue(Rol rol);
}
