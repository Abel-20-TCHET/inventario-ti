package com.example.colegio_ayacucho.exception;

/**
 * Violación de una regla de negocio (fechas incoherentes, borrar un catálogo
 * en uso, eliminar el último administrador, etc.).
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
