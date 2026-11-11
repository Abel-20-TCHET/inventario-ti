package com.example.colegio_ayacucho.controller;

import com.example.colegio_ayacucho.dto.TipoActivoFormDto;
import com.example.colegio_ayacucho.exception.ReglaNegocioException;
import com.example.colegio_ayacucho.model.TipoActivo;
import com.example.colegio_ayacucho.service.TipoActivoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
 * CRUD de tipos de activo. Solo accesible para ADMIN.
 */
@Controller
@RequestMapping("/tipos")
@RequiredArgsConstructor
public class TipoActivoController {

    private final TipoActivoService tipoActivoService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("tipos", tipoActivoService.listar());
        return "tipos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("form", new TipoActivoFormDto());
        model.addAttribute("esEdicion", false);
        return "tipos/form";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("form") TipoActivoFormDto form,
                        BindingResult result,
                        Model model,
                        RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("esEdicion", false);
            return "tipos/form";
        }
        try {
            tipoActivoService.crear(form);
        } catch (ReglaNegocioException ex) {
            result.rejectValue("nombre", "nombre.duplicado", ex.getMessage());
            model.addAttribute("esEdicion", false);
            return "tipos/form";
        }
        redirect.addFlashAttribute("exito", "Tipo de activo creado correctamente.");
        return "redirect:/tipos";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        TipoActivo tipo = tipoActivoService.obtener(id);
        TipoActivoFormDto form = new TipoActivoFormDto();
        form.setId(tipo.getId());
        form.setNombre(tipo.getNombre());
        model.addAttribute("form", form);
        model.addAttribute("esEdicion", true);
        return "tipos/form";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("form") TipoActivoFormDto form,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirect) {
        form.setId(id);
        if (result.hasErrors()) {
            model.addAttribute("esEdicion", true);
            return "tipos/form";
        }
        try {
            tipoActivoService.actualizar(id, form);
        } catch (ReglaNegocioException ex) {
            result.rejectValue("nombre", "nombre.duplicado", ex.getMessage());
            model.addAttribute("esEdicion", true);
            return "tipos/form";
        }
        redirect.addFlashAttribute("exito", "Tipo de activo actualizado correctamente.");
        return "redirect:/tipos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            tipoActivoService.eliminar(id);
            redirect.addFlashAttribute("exito", "Tipo de activo eliminado correctamente.");
        } catch (ReglaNegocioException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/tipos";
    }
}
