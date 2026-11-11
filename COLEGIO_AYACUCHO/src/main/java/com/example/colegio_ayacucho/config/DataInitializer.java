package com.example.colegio_ayacucho.config;

import com.example.colegio_ayacucho.model.Activo;
import com.example.colegio_ayacucho.model.TipoActivo;
import com.example.colegio_ayacucho.model.Ubicacion;
import com.example.colegio_ayacucho.model.Usuario;
import com.example.colegio_ayacucho.model.enums.Criticidad;
import com.example.colegio_ayacucho.model.enums.EstadoActivo;
import com.example.colegio_ayacucho.model.enums.Rol;
import com.example.colegio_ayacucho.repository.ActivoRepository;
import com.example.colegio_ayacucho.repository.TipoActivoRepository;
import com.example.colegio_ayacucho.repository.UbicacionRepository;
import com.example.colegio_ayacucho.repository.UsuarioRepository;
import com.example.colegio_ayacucho.util.CodigoActivoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Carga de datos iniciales. Solo actúa si las tablas están vacías, por lo que
 * es seguro reiniciar la aplicación.
 *
 * Usuarios temporales:
 *   admin    / Admin123*
 *   tecnico1 / Tecnico123*
 * Ambos quedan obligados a cambiar la contraseña en el primer ingreso.
 *
 * Se puede desactivar con: app.seed.enabled=false
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final TipoActivoRepository tipoActivoRepository;
    private final UbicacionRepository ubicacionRepository;
    private final ActivoRepository activoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        crearUsuarios();
        crearTipos();
        crearUbicaciones();
        crearActivos();
    }

    // ------------------------------------------------------------------
    // Usuarios
    // ------------------------------------------------------------------
    private void crearUsuarios() {
        if (usuarioRepository.count() > 0) {
            return;
        }
        usuarioRepository.save(Usuario.builder()
                .username("admin")
                .password(passwordEncoder.encode("Admin123*"))
                .nombreCompleto("Administrador del Sistema")
                .rol(Rol.ADMIN)
                .activo(true)
                .debeCambiarPassword(true)
                .build());

        usuarioRepository.save(Usuario.builder()
                .username("tecnico1")
                .password(passwordEncoder.encode("Tecnico123*"))
                .nombreCompleto("Técnico de Soporte")
                .rol(Rol.TECNICO)
                .activo(true)
                .debeCambiarPassword(true)
                .build());

        log.info("Usuarios iniciales creados: admin / tecnico1 (contraseñas temporales).");
    }

    // ------------------------------------------------------------------
    // Catálogos
    // ------------------------------------------------------------------
    private void crearTipos() {
        if (tipoActivoRepository.count() > 0) {
            return;
        }
        List.of("Servidor", "Switch", "Punto de acceso", "Firewall",
                        "NAS", "UPS", "PC", "Impresora")
                .forEach(nombre -> tipoActivoRepository.save(
                        TipoActivo.builder().nombre(nombre).build()));
    }

    private void crearUbicaciones() {
        if (ubicacionRepository.count() > 0) {
            return;
        }
        List.of("Gabinete principal", "Laboratorio de cómputo", "Pabellón 1",
                        "Pabellón 2", "Pabellón 3", "Administración", "Sala de servidores")
                .forEach(nombre -> ubicacionRepository.save(
                        Ubicacion.builder().nombre(nombre).build()));
    }

    // ------------------------------------------------------------------
    // Activos de ejemplo (45 equipos: CI-001 ... CI-045)
    // Las fechas se calculan respecto de hoy para que las alertas de
    // garantía (rojo / amarillo / verde) siempre se puedan probar.
    // ------------------------------------------------------------------
    private void crearActivos() {
        if (activoRepository.count() > 0) {
            return;
        }

        Map<String, TipoActivo> tipos = tipoActivoRepository.findAll().stream()
                .collect(Collectors.toMap(t -> t.getNombre().toLowerCase(Locale.ROOT),
                        Function.identity()));
        Map<String, Ubicacion> ubicaciones = ubicacionRepository.findAll().stream()
                .collect(Collectors.toMap(u -> u.getNombre().toLowerCase(Locale.ROOT),
                        Function.identity()));

        LocalDate hoy = LocalDate.now();
        List<Activo> lista = new ArrayList<>();

        // --- Infraestructura de red principal ---
        lista.add(activo("CI-001", "Firewall perimetral NGFW", tipos.get("firewall"),
                "Fortinet", "FortiGate 100F", "FGT100F-2021-001",
                ubicaciones.get("gabinete principal"), EstadoActivo.OPERATIVO, Criticidad.ALTA,
                hoy.minusYears(4), hoy.minusMonths(8),
                "Equipo de borde de red. Licencia UTM pendiente de renovación."));

        lista.add(activo("CI-002", "Switch core capa 3", tipos.get("switch"),
                "Cisco", "Catalyst 9300-48P", "FOC2401X0A1",
                ubicaciones.get("sala de servidores"), EstadoActivo.OPERATIVO, Criticidad.ALTA,
                hoy.minusYears(2), hoy.plusDays(45),
                "Núcleo de la red del colegio."));

        // --- 6 switches de acceso SW-A1 ... SW-A6 (CI-003 ... CI-008) ---
        String[] switches = {"SW-A1", "SW-A2", "SW-A3", "SW-A4", "SW-A5", "SW-A6"};
        String[] ubicacionSwitch = {"Pabellón 1", "Pabellón 1", "Pabellón 2",
                "Pabellón 2", "Pabellón 3", "Pabellón 3"};
        for (int i = 0; i < switches.length; i++) {
            lista.add(activo(CodigoActivoUtil.formatear(3L + i),
                    "Switch de acceso " + switches[i], tipos.get("switch"),
                    "Cisco", "Catalyst 9200-24P", "SWA" + (i + 1) + "-2401",
                    ubicaciones.get(ubicacionSwitch[i].toLowerCase(Locale.ROOT)),
                    i == 3 ? EstadoActivo.EN_REPARACION : EstadoActivo.OPERATIVO,
                    Criticidad.MEDIA, hoy.minusYears(2), garantiaVariada(hoy, i),
                    "Distribución de red del pabellón."));
        }

        // --- 32 puntos de acceso Wi-Fi 6 AP-01 ... AP-32 (CI-009 ... CI-040) ---
        String[] ubicacionAp = {"Pabellón 1", "Pabellón 2", "Pabellón 3",
                "Laboratorio de cómputo", "Administración", "Sala de servidores"};
        for (int i = 1; i <= 32; i++) {
            EstadoActivo estado = EstadoActivo.OPERATIVO;
            if (i == 15) {
                estado = EstadoActivo.EN_REPARACION;
            } else if (i == 21 || i == 22) {
                estado = EstadoActivo.DE_BAJA;
            }
            lista.add(activo(CodigoActivoUtil.formatear(8L + i),
                    String.format("Punto de acceso Wi-Fi 6 AP-%02d", i),
                    tipos.get("punto de acceso"), "Ubiquiti", "UniFi U6-Pro",
                    String.format("U6P-%04d", i),
                    ubicaciones.get(ubicacionAp[i % ubicacionAp.length].toLowerCase(Locale.ROOT)),
                    estado, i <= 8 ? Criticidad.ALTA : Criticidad.MEDIA,
                    hoy.minusYears(2), garantiaVariada(hoy, i),
                    "Cobertura Wi-Fi de aulas y patios."));
        }

        // --- Servidores host (CI-041 y CI-042) ---
        lista.add(activo("CI-041", "Servidor host HOST-A (virtualización)", tipos.get("servidor"),
                "Dell", "PowerEdge R750", "PE750-A-001",
                ubicaciones.get("sala de servidores"), EstadoActivo.OPERATIVO, Criticidad.ALTA,
                hoy.minusYears(1), hoy.plusYears(3),
                "Host principal de máquinas virtuales."));

        lista.add(activo("CI-042", "Servidor host HOST-B (respaldo)", tipos.get("servidor"),
                "Dell", "PowerEdge R750", "PE750-B-002",
                ubicaciones.get("sala de servidores"), EstadoActivo.OPERATIVO, Criticidad.ALTA,
                hoy.minusYears(1), hoy.plusDays(30),
                "Host secundario, réplica del HOST-A."));

        // --- NAS de 12 TB útiles (CI-043) ---
        lista.add(activo("CI-043", "NAS 12 TB útiles", tipos.get("nas"),
                "Synology", "RS1221+", "SYN-RS1221-001",
                ubicaciones.get("sala de servidores"), EstadoActivo.OPERATIVO, Criticidad.ALTA,
                hoy.minusYears(2), hoy.plusMonths(10),
                "Respaldo de documentos académicos y administrativos."));

        // --- 2 UPS online de 3 kVA (CI-044 y CI-045) ---
        lista.add(activo("CI-044", "UPS online 3 kVA - rack principal", tipos.get("ups"),
                "APC", "Smart-UPS SRT 3000VA", "UPS-SRT-001",
                ubicaciones.get("gabinete principal"), EstadoActivo.OPERATIVO, Criticidad.ALTA,
                hoy.minusYears(4), hoy.minusMonths(2),
                "Baterías con más de 3 años de uso; evaluar reemplazo."));

        lista.add(activo("CI-045", "UPS online 3 kVA - sala de servidores", tipos.get("ups"),
                "APC", "Smart-UPS SRT 3000VA", "UPS-SRT-002",
                ubicaciones.get("sala de servidores"), EstadoActivo.EN_REPARACION, Criticidad.ALTA,
                hoy.minusYears(3), hoy.plusDays(20),
                "En revisión por falla en el módulo de baterías."));

        activoRepository.saveAll(lista);
        log.info("Activos de ejemplo cargados: {} equipos (CI-001 a {}).",
                lista.size(), CodigoActivoUtil.formatear(lista.size()));
    }

    /**
     * Garantías variadas de forma determinística: vencidas, por vencer,
     * vigentes y sin registro, para probar el semáforo del listado.
     */
    private LocalDate garantiaVariada(LocalDate hoy, int i) {
        return switch (i % 6) {
            case 0 -> hoy.minusMonths(5);   // vencida
            case 1 -> hoy.plusDays(25);     // por vencer
            case 2 -> hoy.plusDays(58);     // por vencer
            case 3 -> hoy.plusYears(2);     // vigente
            case 4 -> hoy.plusMonths(14);   // vigente
            default -> null;                // sin garantía registrada
        };
    }

    private Activo activo(String codigo, String nombre, TipoActivo tipo,
                          String marca, String modelo, String serie,
                          Ubicacion ubicacion, EstadoActivo estado, Criticidad criticidad,
                          LocalDate fechaCompra, LocalDate fechaFinGarantia, String observaciones) {
        Activo a = new Activo();
        a.setCodigo(codigo);
        a.setNombre(nombre);
        a.setTipo(tipo);
        a.setMarca(marca);
        a.setModelo(modelo);
        a.setNumeroSerie(serie);
        a.setUbicacion(ubicacion);
        a.setResponsable("Área de TI");
        a.setEstado(estado);
        a.setCriticidad(criticidad);
        a.setFechaCompra(fechaCompra);
        a.setFechaFinGarantia(fechaFinGarantia);
        a.setObservaciones(observaciones);
        return a;
    }
}
