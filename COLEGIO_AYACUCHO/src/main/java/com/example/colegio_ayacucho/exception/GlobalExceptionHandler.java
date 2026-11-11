package com.example.colegio_ayacucho.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Manejo global de errores: muestra páginas amigables en español en lugar del
 * error técnico de Spring.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ModelAndView noEncontrado(RecursoNoEncontradoException ex, HttpServletRequest request) {
        return pagina(HttpStatus.NOT_FOUND, "error/404", ex.getMessage(), request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ModelAndView recursoInexistente(NoResourceFoundException ex, HttpServletRequest request) {
        return pagina(HttpStatus.NOT_FOUND, "error/404",
                "La página o el recurso solicitado no existe.", request);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ModelAndView reglaNegocio(ReglaNegocioException ex, HttpServletRequest request) {
        return pagina(HttpStatus.BAD_REQUEST, "error/400", ex.getMessage(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ModelAndView integridad(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Violación de integridad de datos: {}", ex.getMostSpecificCause().getMessage());
        return pagina(HttpStatus.CONFLICT, "error/400",
                "La operación no se pudo completar porque el dato está relacionado con otros registros.", request);
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView errorGeneral(Exception ex, HttpServletRequest request) {
        log.error("Error no controlado en {}", request.getRequestURI(), ex);
        return pagina(HttpStatus.INTERNAL_SERVER_ERROR, "error/500",
                "Ocurrió un error inesperado. Intente nuevamente o contacte al área de TI.", request);
    }

    private ModelAndView pagina(HttpStatus estado, String vista, String mensaje,
                                HttpServletRequest request) {
        ModelAndView mav = new ModelAndView(vista);
        mav.setStatus(estado);
        mav.addObject("mensaje", mensaje);
        mav.addObject("ruta", request.getRequestURI());
        return mav;
    }
}
