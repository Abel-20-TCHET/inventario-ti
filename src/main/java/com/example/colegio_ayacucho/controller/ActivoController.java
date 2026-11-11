package com.example.colegio_ayacucho.controller;

import com.example.colegio_ayacucho.dto.ActivoFiltroDto;
import com.example.colegio_ayacucho.dto.ActivoFormDto;
import com.example.colegio_ayacucho.exception.CodigoDuplicadoException;
import com.example.colegio_ayacucho.exception.ReglaNegocioException;
import com.example.colegio_ayacucho.model.Activo;
import com.example.colegio_ayacucho.model.enums.Criticidad;
import com.example.colegio_ayacucho.model.enums.EstadoActivo;
import com.example.colegio_ayacucho.service.ActivoService;
import com.example.colegio_ayacucho.service.CsvService;
import com.example.colegio_ayacucho.service.HistorialService;
import com.example.colegio_ayacucho.service.TipoActivoService;
import com.example.colegio_ayacucho.service.UbicacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

import java.time.LocalDate;

/**
 * CRUD de activos + buscador, filtros, paginación y exportación CSV.
 */
@Controller
@RequestMapping("/activos")
@RequiredArgsConstructor
public class ActivoController {

    private static final int TAMANO_PAGINA = 10;

    private final ActivoService activoService;
    private final HistorialService historialService;
    private final TipoActivoService tipoActivoService;
    private final UbicacionService ubicacionService;
    private final CsvService csvService;

    // ------------------------------------------------------------------
    // Listado con filtros y paginación
    // ------------------------------------------------------------------
    @GetMapping
    public String listar(@ModelAttribute("filtro") ActivoFiltroDto filtro, Model model) {
        Page<Activo> pagina = activoService.buscar(filtro,
                PageRequest.of(filtro.getPage(), TAMANO_PAGINA, Sort.by("codigo").ascending()));
        model.addAttribute("pagina", pagina);
        cargarCatalogos(model);
        return "activos/lista";
    }

    /** Exporta exactamente el resultado del filtro actual. */
    @GetMapping("/exportar")
    public ResponseEntity<byte[]> exportar(@ModelAttribute("filtro") ActivoFiltroDto filtro) {
        byte[] contenido = csvService.exportarActivos(activoService.listarParaExportar(filtro));
        String archivo = "inventario_activos_" + LocalDate.now() + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + archivo + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(contenido);
    }

    // ------------------------------------------------------------------
    // Alta
    // ------------------------------------------------------------------
    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        ActivoFormDto form = new ActivoFormDto();
        form.setCodigo(activoService.sugerirCodigo());
        form.setFechaCompra(LocalDate.now());
        return formulario(form, model, false);
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("form") ActivoFormDto form,
                        BindingResult result,
                        @AuthenticationPrincipal UserDetails usuario,
                        Model model,
                        RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return formulario(form, model, false);
        }
        try {
            activoService.crear(form, usuario.getUsername());
        } catch (CodigoDuplicadoException ex) {
            result.rejectValue("codigo", "codigo.duplicado", ex.getMessage());
            return formulario(form, model, false);
        } catch (ReglaNegocioException ex) {
            result.reject("errorGeneral", ex.getMessage());
            return formulario(form, model, false);
        }
        redirect.addFlashAttribute("exito",
                "Activo " + form.getCodigo() + " registrado correctamente.");
        return "redirect:/activos";
    }

    // ------------------------------------------------------------------
    // Detalle e historial
    // ------------------------------------------------------------------
    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        Activo activo = activoService.obtener(id);
        model.addAttribute("activo", activo);
        model.addAttribute("historial", historialService.listarPorActivo(id));
        return "activos/detalle";
    }

    // ------------------------------------------------------------------
    // Edición
    // ------------------------------------------------------------------
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Activo activo = activoService.obtener(id);
        return formulario(aFormulario(activo), model, true);
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("form") ActivoFormDto form,
                             BindingResult result,
                             @AuthenticationPrincipal UserDetails usuario,
                             Model model,
                             RedirectAttributes redirect) {
        form.setId(id);
        if (result.hasErrors()) {
            return formulario(form, model, true);
        }
        try {
            activoService.actualizar(id, form, usuario.getUsername());
        } catch (CodigoDuplicadoException ex) {
            result.rejectValue("codigo", "codigo.duplicado", ex.getMessage());
            return formulario(form, model, true);
        } catch (ReglaNegocioException ex) {
            result.reject("errorGeneral", ex.getMessage());
            return formulario(form, model, true);
        }
        redirect.addFlashAttribute("exito",
                "Activo " + form.getCodigo() + " actualizado correctamente.");
        return "redirect:/activos/" + id;
    }

    // ------------------------------------------------------------------
    // Baja (solo ADMIN: se controla también en SecurityConfig)
    // ------------------------------------------------------------------
    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id,
                           @AuthenticationPrincipal UserDetails usuario,
                           RedirectAttributes redirect) {
        Activo activo = activoService.obtener(id);
        String codigo = activo.getCodigo();
        activoService.eliminar(id, usuario.getUsername());
        redirect.addFlashAttribute("exito", "Activo " + codigo + " eliminado correctamente.");
        return "redirect:/activos";
    }

    // ------------------------------------------------------------------
    // Apoyo
    // ------------------------------------------------------------------
    private String formulario(ActivoFormDto form, Model model, boolean esEdicion) {
        model.addAttribute("form", form);
        model.addAttribute("esEdicion", esEdicion);
        cargarCatalogos(model);
        return "activos/form";
    }

    private void cargarCatalogos(Model model) {
        model.addAttribute("tipos", tipoActivoService.listar());
        model.addAttribute("ubicaciones", ubicacionService.listar());
        model.addAttribute("estados", EstadoActivo.values());
        model.addAttribute("criticidades", Criticidad.values());
    }

    private ActivoFormDto aFormulario(Activo a) {
        ActivoFormDto dto = new ActivoFormDto();
        dto.setId(a.getId());
        dto.setCodigo(a.getCodigo());
        dto.setNombre(a.getNombre());
        dto.setTipoId(a.getTipo().getId());
        dto.setMarca(a.getMarca());
        dto.setModelo(a.getModelo());
        dto.setNumeroSerie(a.getNumeroSerie());
        dto.setUbicacionId(a.getUbicacion().getId());
        dto.setResponsable(a.getResponsable());
        dto.setEstado(a.getEstado());
        dto.setCriticidad(a.getCriticidad());
        dto.setFechaCompra(a.getFechaCompra());
        dto.setFechaFinGarantia(a.getFechaFinGarantia());
        dto.setObservaciones(a.getObservaciones());
        return dto;
    }
}
