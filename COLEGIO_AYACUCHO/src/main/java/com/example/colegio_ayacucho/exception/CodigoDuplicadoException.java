package com.example.colegio_ayacucho.exception;

/**
 * Código de activo repetido. En los formularios se muestra junto al campo.
 */
public class CodigoDuplicadoException extends RuntimeException {

    public CodigoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
