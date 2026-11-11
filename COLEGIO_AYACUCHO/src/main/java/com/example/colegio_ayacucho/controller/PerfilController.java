package com.example.colegio_ayacucho.controller;

import com.example.colegio_ayacucho.config.CambioPasswordInterceptor;
import com.example.colegio_ayacucho.dto.CambioPasswordDto;
import com.example.colegio_ayacucho.exception.ReglaNegocioException;
import com.example.colegio_ayacucho.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Cambio de contraseña del usuario autenticado. Es la única pantalla a la que
 * puede acceder un usuario con contraseña temporal.
 */
@Controller
@RequestMapping("/perfil")
@RequiredArgsConstructor
public class PerfilController {

    private final UsuarioService usuarioService;

    @GetMapping("/cambiar-password")
    public String formulario(Model model) {
        model.addAttribute("form", new CambioPasswordDto());
        return "perfil/cambiar-password";
    }

    @PostMapping("/cambiar-password")
    public String cambiar(@Valid @ModelAttribute("form") CambioPasswordDto form,
                          BindingResult result,
                          @AuthenticationPrincipal UserDetails usuario,
                          Model model,
                          HttpSession session,
                          RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "perfil/cambiar-password";
        }
        try {
            usuarioService.cambiarPassword(usuario.getUsername(), form);
        } catch (ReglaNegocioException ex) {
            result.reject("errorGeneral", ex.getMessage());
            return "perfil/cambiar-password";
        }

        // Ya no se le exige el cambio en esta sesión
        session.removeAttribute(CambioPasswordInterceptor.SESSION_FLAG);
        redirect.addFlashAttribute("exito", "Contraseña actualizada correctamente.");
        return "redirect:/dashboard";
    }
}
