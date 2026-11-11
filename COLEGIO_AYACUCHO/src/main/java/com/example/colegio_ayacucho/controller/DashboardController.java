package com.example.colegio_ayacucho.controller;

import com.example.colegio_ayacucho.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private static final Locale ES_PE = Locale.forLanguageTag("es-PE");
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy", ES_PE);
    private static final DateTimeFormatter FORMATO_HORA =
            DateTimeFormatter.ofPattern("HH:mm", ES_PE);

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("resumen", dashboardService.obtenerResumen());
        model.addAttribute("fechaHoy", fechaEnEspanol(LocalDate.now()));
        model.addAttribute("horaActual", LocalTime.now().format(FORMATO_HORA));
        return "dashboard";
    }

    /** "Lunes 9 de noviembre de 2026" (con la primera letra en mayúscula). */
    private String fechaEnEspanol(LocalDate fecha) {
        String texto = fecha.format(FORMATO_FECHA);
        return texto.isEmpty() ? texto
                : Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }
}
