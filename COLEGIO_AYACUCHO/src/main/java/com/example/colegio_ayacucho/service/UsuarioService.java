package com.example.colegio_ayacucho.service;

import com.example.colegio_ayacucho.dto.CambioPasswordDto;
import com.example.colegio_ayacucho.dto.UsuarioFormDto;
import com.example.colegio_ayacucho.exception.RecursoNoEncontradoException;
import com.example.colegio_ayacucho.exception.ReglaNegocioException;
import com.example.colegio_ayacucho.model.Usuario;
import com.example.colegio_ayacucho.model.enums.Rol;
import com.example.colegio_ayacucho.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Gestión de usuarios (solo ADMIN) y cambio de contraseña del usuario actual.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return usuarioRepository.findAllByOrderByNombreCompletoAsc();
    }

    @Transactional(readOnly = true)
    public Usuario obtener(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el usuario con ID " + id + "."));
    }

    @Transactional(readOnly = true)
    public Usuario porUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado."));
    }

    public Usuario crear(UsuarioFormDto dto) {
        String username = dto.getUsername().trim();
        if (usuarioRepository.existsByUsernameIgnoreCase(username)) {
            throw new ReglaNegocioException("El nombre de usuario '" + username + "' ya está en uso.");
        }
        if (!StringUtils.hasText(dto.getPassword())) {
            throw new ReglaNegocioException("La contraseña es obligatoria para un usuario nuevo.");
        }

        Usuario usuario = Usuario.builder()
                .username(username)
                .password(passwordEncoder.encode(dto.getPassword()))
                .nombreCompleto(dto.getNombreCompleto().trim())
                .rol(dto.getRol())
                .activo(dto.isActivo())
                .debeCambiarPassword(true)   // contraseña temporal: se cambia al primer ingreso
                .build();
        return usuarioRepository.save(usuario);
    }

    public Usuario actualizar(Long id, UsuarioFormDto dto, String usernameActual) {
        Usuario usuario = obtener(id);
        String username = dto.getUsername().trim();

        if (usuarioRepository.existsByUsernameIgnoreCaseAndIdNot(username, id)) {
            throw new ReglaNegocioException("El nombre de usuario '" + username + "' ya está en uso.");
        }
        boolean esUnoMismo = usuario.getUsername().equalsIgnoreCase(usernameActual);
        if (esUnoMismo && !dto.isActivo()) {
            throw new ReglaNegocioException("No puede desactivar su propio usuario.");
        }
        // Nunca dejar el sistema sin al menos un ADMIN activo
        if (usuario.isActivo() && usuario.getRol() == Rol.ADMIN
                && (!dto.isActivo() || dto.getRol() != Rol.ADMIN)
                && usuarioRepository.countByRolAndActivoTrue(Rol.ADMIN) <= 1) {
            throw new ReglaNegocioException(
                    "Debe existir al menos un administrador activo en el sistema.");
        }

        usuario.setUsername(username);
        usuario.setNombreCompleto(dto.getNombreCompleto().trim());
        usuario.setRol(dto.getRol());
        usuario.setActivo(dto.isActivo());

        // Si el ADMIN escribe una clave nueva, se cifra y se obliga a cambiarla
        if (StringUtils.hasText(dto.getPassword())) {
            usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
            usuario.setDebeCambiarPassword(true);
        }
        return usuarioRepository.save(usuario);
    }

    public void eliminar(Long id, String usernameActual) {
        Usuario usuario = obtener(id);
        if (usuario.getUsername().equalsIgnoreCase(usernameActual)) {
            throw new ReglaNegocioException("No puede eliminar su propio usuario.");
        }
        if (usuario.getRol() == Rol.ADMIN && usuarioRepository.countByRolAndActivoTrue(Rol.ADMIN) <= 1) {
            throw new ReglaNegocioException(
                    "Debe existir al menos un administrador activo en el sistema.");
        }
        usuarioRepository.delete(usuario);
    }

    /** Cambio de contraseña del usuario autenticado. */
    public void cambiarPassword(String username, CambioPasswordDto dto) {
        Usuario usuario = porUsername(username);

        if (!passwordEncoder.matches(dto.getActual(), usuario.getPassword())) {
            throw new ReglaNegocioException("La contraseña actual no es correcta.");
        }
        if (!dto.getNueva().equals(dto.getConfirmar())) {
            throw new ReglaNegocioException("La confirmación no coincide con la nueva contraseña.");
        }
        if (passwordEncoder.matches(dto.getNueva(), usuario.getPassword())) {
            throw new ReglaNegocioException("La nueva contraseña debe ser distinta a la actual.");
        }

        usuario.setPassword(passwordEncoder.encode(dto.getNueva()));
        usuario.setDebeCambiarPassword(false);
        usuarioRepository.save(usuario);
    }
}
