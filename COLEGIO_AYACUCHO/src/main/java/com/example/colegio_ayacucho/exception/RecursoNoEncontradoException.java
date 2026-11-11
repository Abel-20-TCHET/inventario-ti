package com.example.colegio_ayacucho.exception;

/**
 * Se lanza cuando no existe la entidad solicitada. Se traduce a HTTP 404.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
