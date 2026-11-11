package com.example.colegio_ayacucho.config;

import com.example.colegio_ayacucho.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Tras un login correcto:
 *  - si la cuenta tiene contraseña temporal, marca la sesión para obligar el cambio;
 *  - redirige al dashboard.
 */
@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UsuarioRepository usuarioRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        HttpSession session = request.getSession(true);

        usuarioRepository.findByUsername(authentication.getName()).ifPresent(usuario -> {
            if (usuario.isDebeCambiarPassword()) {
                session.setAttribute(CambioPasswordInterceptor.SESSION_FLAG, Boolean.TRUE);
            } else {
                session.removeAttribute(CambioPasswordInterceptor.SESSION_FLAG);
            }
        });

        response.sendRedirect(request.getContextPath() + "/dashboard");
    }
}
