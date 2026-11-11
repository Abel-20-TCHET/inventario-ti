package com.example.colegio_ayacucho.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Página 403 usada por Spring Security cuando un TECNICO intenta acceder a
 * una sección exclusiva del ADMIN.
 */
@Controller
public class ErrorPageController {

    @GetMapping("/error/403")
    public String accesoDenegado() {
        return "error/403";
    }
}
