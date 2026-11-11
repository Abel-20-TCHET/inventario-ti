package com.example.colegio_ayacucho.controller;

import com.example.colegio_ayacucho.dto.UbicacionFormDto;
import com.example.colegio_ayacucho.exception.ReglaNegocioException;
import com.example.colegio_ayacucho.model.Ubicacion;
import com.example.colegio_ayacucho.service.UbicacionService;
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
 * CRUD de ubicaciones. Solo accesible para ADMIN.
 */
@Controller
@RequestMapping("/ubicaciones")
@RequiredArgsConstructor
public class UbicacionController {

    private final UbicacionService ubicacionService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("ubicaciones", ubicacionService.listar());
        return "ubicaciones/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("form", new UbicacionFormDto());
        model.addAttribute("esEdicion", false);
        return "ubicaciones/form";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("form") UbicacionFormDto form,
                        BindingResult result,
                        Model model,
                        RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("esEdicion", false);
            return "ubicaciones/form";
        }
        try {
            ubicacionService.crear(form);
        } catch (ReglaNegocioException ex) {
            result.rejectValue("nombre", "nombre.duplicado", ex.getMessage());
            model.addAttribute("esEdicion", false);
            return "ubicaciones/form";
        }
        redirect.addFlashAttribute("exito", "Ubicación creada correctamente.");
        return "redirect:/ubicaciones";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Ubicacion ubicacion = ubicacionService.obtener(id);
        UbicacionFormDto form = new UbicacionFormDto();
        form.setId(ubicacion.getId());
        form.setNombre(ubicacion.getNombre());
        model.addAttribute("form", form);
        model.addAttribute("esEdicion", true);
        return "ubicaciones/form";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("form") UbicacionFormDto form,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirect) {
        form.setId(id);
        if (result.hasErrors()) {
            model.addAttribute("esEdicion", true);
            return "ubicaciones/form";
        }
        try {
            ubicacionService.actualizar(id, form);
        } catch (ReglaNegocioException ex) {
            result.rejectValue("nombre", "nombre.duplicado", ex.getMessage());
            model.addAttribute("esEdicion", true);
            return "ubicaciones/form";
        }
        redirect.addFlashAttribute("exito", "Ubicación actualizada correctamente.");
        return "redirect:/ubicaciones";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            ubicacionService.eliminar(id);
            redirect.addFlashAttribute("exito", "Ubicación eliminada correctamente.");
        } catch (ReglaNegocioException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/ubicaciones";
    }
}
