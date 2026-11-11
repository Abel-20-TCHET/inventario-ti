package com.example.colegio_ayacucho.controller;

import com.example.colegio_ayacucho.dto.UsuarioFormDto;
import com.example.colegio_ayacucho.exception.ReglaNegocioException;
import com.example.colegio_ayacucho.model.Usuario;
import com.example.colegio_ayacucho.model.enums.Rol;
import com.example.colegio_ayacucho.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Gestión de usuarios. Solo accesible para ADMIN.
 */
@Controller
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listar());
        return "usuarios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("form", new UsuarioFormDto());
        model.addAttribute("roles", Rol.values());
        model.addAttribute("esEdicion", false);
        return "usuarios/form";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("form") UsuarioFormDto form,
                        BindingResult result,
                        Model model,
                        RedirectAttributes redirect) {
        if (form.getPassword() == null || form.getPassword().isBlank()) {
            result.rejectValue("password", "password.obligatoria",
                    "La contraseña es obligatoria para un usuario nuevo.");
        }
        if (result.hasErrors()) {
            return formulario(form, model, false);
        }
        try {
            usuarioService.crear(form);
        } catch (ReglaNegocioException ex) {
            result.rejectValue("username", "username.duplicado", ex.getMessage());
            return formulario(form, model, false);
        }
        redirect.addFlashAttribute("exito",
                "Usuario creado. Deberá cambiar la contraseña en su primer ingreso.");
        return "redirect:/usuarios";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioService.obtener(id);
        UsuarioFormDto form = new UsuarioFormDto();
        form.setId(usuario.getId());
        form.setUsername(usuario.getUsername());
        form.setNombreCompleto(usuario.getNombreCompleto());
        form.setRol(usuario.getRol());
        form.setActivo(usuario.isActivo());
        return formulario(form, model, true);
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("form") UsuarioFormDto form,
                             BindingResult result,
                             @AuthenticationPrincipal UserDetails usuarioActual,
                             Model model,
                             RedirectAttributes redirect) {
        form.setId(id);
        if (result.hasErrors()) {
            return formulario(form, model, true);
        }
        try {
            usuarioService.actualizar(id, form, usuarioActual.getUsername());
        } catch (ReglaNegocioException ex) {
            result.reject("errorGeneral", ex.getMessage());
            return formulario(form, model, true);
        }
        redirect.addFlashAttribute("exito", "Usuario actualizado correctamente.");
        return "redirect:/usuarios";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id,
                           @AuthenticationPrincipal UserDetails usuarioActual,
                           RedirectAttributes redirect) {
        try {
            usuarioService.eliminar(id, usuarioActual.getUsername());
            redirect.addFlashAttribute("exito", "Usuario eliminado correctamente.");
        } catch (ReglaNegocioException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/usuarios";
    }

    private String formulario(UsuarioFormDto form, Model model, boolean esEdicion) {
        model.addAttribute("form", form);
        model.addAttribute("roles", Rol.values());
        model.addAttribute("esEdicion", esEdicion);
        return "usuarios/form";
    }
}
