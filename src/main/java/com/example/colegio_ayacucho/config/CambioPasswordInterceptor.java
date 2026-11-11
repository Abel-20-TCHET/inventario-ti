package com.example.colegio_ayacucho.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Bloquea la navegación mientras el usuario tenga una contraseña temporal:
 * lo redirige siempre a /perfil/cambiar-password hasta que la cambie.
 */
@Component
public class CambioPasswordInterceptor implements HandlerInterceptor {

    public static final String SESSION_FLAG = "DEBE_CAMBIAR_PASSWORD";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SESSION_FLAG) == null) {
            return true;
        }

        String ruta = request.getRequestURI().substring(request.getContextPath().length());
        if (ruta.equals("/perfil/cambiar-password")
                || ruta.equals("/logout")
                || ruta.startsWith("/css/")
                || ruta.startsWith("/js/")
                || ruta.startsWith("/error/")) {
            return true;
        }

        response.sendRedirect(request.getContextPath() + "/perfil/cambiar-password");
        return false;
    }
}
