package com.example.colegio_ayacucho.service;

import com.example.colegio_ayacucho.model.Activo;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Exportación del listado filtrado a CSV.
 *
 * Detalles pensados para Excel en español (Perú):
 *  - se antepone BOM UTF-8 (\uFEFF) para que respete las tildes;
 *  - se usa ";" como separador (configuración regional es-PE);
 *  - los campos con ; " o saltos de línea se entrecomillan.
 */
@Service
public class CsvService {

    private static final char SEPARADOR = ';';
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("es-PE"));

    public byte[] exportarActivos(List<Activo> activos) {
        StringBuilder sb = new StringBuilder(2048);
        sb.append('\uFEFF');
        sb.append("Código;Nombre;Tipo;Marca;Modelo;N° de serie;Ubicación;Responsable;"
                + "Estado;Criticidad;Fecha de compra;Fin de garantía;Estado de garantía;Observaciones\r\n");

        for (Activo a : activos) {
            sb.append(campo(a.getCodigo())).append(SEPARADOR)
              .append(campo(a.getNombre())).append(SEPARADOR)
              .append(campo(a.getTipo().getNombre())).append(SEPARADOR)
              .append(campo(a.getMarca())).append(SEPARADOR)
              .append(campo(a.getModelo())).append(SEPARADOR)
              .append(campo(a.getNumeroSerie())).append(SEPARADOR)
              .append(campo(a.getUbicacion().getNombre())).append(SEPARADOR)
              .append(campo(a.getResponsable())).append(SEPARADOR)
              .append(campo(a.getEstado().getEtiqueta())).append(SEPARADOR)
              .append(campo(a.getCriticidad().getEtiqueta())).append(SEPARADOR)
              .append(a.getFechaCompra() == null ? "" : a.getFechaCompra().format(FORMATO_FECHA))
              .append(SEPARADOR)
              .append(a.getFechaFinGarantia() == null ? ""
                      : a.getFechaFinGarantia().format(FORMATO_FECHA))
              .append(SEPARADOR)
              .append(campo(a.getEstadoGarantia().getEtiqueta())).append(SEPARADOR)
              .append(campo(a.getObservaciones()))
              .append("\r\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String campo(String valor) {
        if (valor == null) {
            return "";
        }
        String limpio = valor.replace("\r", " ").replace("\n", " ").trim();
        if (limpio.indexOf(SEPARADOR) >= 0 || limpio.contains("\"") || limpio.contains(",")) {
            return "\"" + limpio.replace("\"", "\"\"") + "\"";
        }
        return limpio;
    }
}
