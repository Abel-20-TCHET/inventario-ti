package com.example.colegio_ayacucho.service;

import com.example.colegio_ayacucho.dto.ActivoFormDto;
import com.example.colegio_ayacucho.exception.CodigoDuplicadoException;
import com.example.colegio_ayacucho.exception.ReglaNegocioException;
import com.example.colegio_ayacucho.model.Activo;
import com.example.colegio_ayacucho.model.TipoActivo;
import com.example.colegio_ayacucho.model.Ubicacion;
import com.example.colegio_ayacucho.model.enums.AccionHistorial;
import com.example.colegio_ayacucho.model.enums.Criticidad;
import com.example.colegio_ayacucho.model.enums.EstadoActivo;
import com.example.colegio_ayacucho.repository.ActivoRepository;
import com.example.colegio_ayacucho.repository.TipoActivoRepository;
import com.example.colegio_ayacucho.repository.UbicacionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias del servicio de activos con Mockito (sin base de datos).
 */
@ExtendWith(MockitoExtension.class)
class ActivoServiceTest {

    @Mock
    private ActivoRepository activoRepository;

    @Mock
    private TipoActivoRepository tipoActivoRepository;

    @Mock
    private UbicacionRepository ubicacionRepository;

    @Mock
    private HistorialService historialService;

    @InjectMocks
    private ActivoService activoService;

    // ------------------------------------------------------------------
    // Alta
    // ------------------------------------------------------------------
    @Test
    @DisplayName("No se permite registrar un activo con un código duplicado")
    void crearConCodigoDuplicadoLanzaExcepcion() {
        ActivoFormDto dto = dto("CI-001", LocalDate.now(), LocalDate.now().plusYears(1));
        when(activoRepository.existsByCodigoIgnoreCase("CI-001")).thenReturn(true);

        CodigoDuplicadoException ex = assertThrows(CodigoDuplicadoException.class,
                () -> activoService.crear(dto, "admin"));

        assertEquals("El código CI-001 ya está registrado. Ingrese un código distinto.",
                ex.getMessage());
        verify(activoRepository, never()).save(any(Activo.class));
        verifyNoInteractions(historialService);
    }

    @Test
    @DisplayName("Al crear un activo válido se guarda y se registra el historial")
    void crearActivoValidoRegistraHistorial() {
        ActivoFormDto dto = dto("CI-100", LocalDate.now().minusYears(1), LocalDate.now().plusYears(1));
        when(activoRepository.existsByCodigoIgnoreCase("CI-100")).thenReturn(false);
        when(tipoActivoRepository.findById(1L)).thenReturn(Optional.of(
                TipoActivo.builder().id(1L).nombre("Switch").build()));
        when(ubicacionRepository.findById(2L)).thenReturn(Optional.of(
                Ubicacion.builder().id(2L).nombre("Sala de servidores").build()));
        when(activoRepository.save(any(Activo.class))).thenAnswer(inv -> inv.getArgument(0));

        Activo creado = activoService.crear(dto, "admin");

        assertEquals("CI-100", creado.getCodigo());
        assertEquals("Switch", creado.getTipo().getNombre());
        assertEquals(EstadoActivo.OPERATIVO, creado.getEstado());
        verify(historialService).registrar(any(Activo.class), eq("admin"),
                eq(AccionHistorial.CREACION), contains("CI-100"));
    }

    // ------------------------------------------------------------------
    // Regla de fechas
    // ------------------------------------------------------------------
    @Test
    @DisplayName("La garantía no puede terminar antes de la fecha de compra")
    void actualizarConFechasIncoherentesLanzaExcepcion() {
        ActivoFormDto dto = dto("CI-101",
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 9));

        assertThrows(ReglaNegocioException.class,
                () -> activoService.actualizar(1L, dto, "admin"));

        verify(activoRepository, never()).save(any(Activo.class));
        verifyNoInteractions(historialService);
    }

    // ------------------------------------------------------------------
    // Baja
    // ------------------------------------------------------------------
    @Test
    @DisplayName("Al eliminar un activo se registra la acción y luego se borra")
    void eliminarRegistraHistorial() {
        Activo activo = new Activo();
        activo.setId(7L);
        activo.setCodigo("CI-007");
        activo.setNombre("Switch de acceso SW-A5");
        when(activoRepository.findDetalleById(7L)).thenReturn(Optional.of(activo));

        activoService.eliminar(7L, "admin");

        verify(historialService).registrar(eq(activo), eq("admin"),
                eq(AccionHistorial.ELIMINACION), anyString());
        verify(activoRepository).delete(activo);
    }

    // ------------------------------------------------------------------
    // Apoyo
    // ------------------------------------------------------------------
    private ActivoFormDto dto(String codigo, LocalDate compra, LocalDate finGarantia) {
        ActivoFormDto dto = new ActivoFormDto();
        dto.setCodigo(codigo);
        dto.setNombre("Equipo de prueba");
        dto.setTipoId(1L);
        dto.setUbicacionId(2L);
        dto.setEstado(EstadoActivo.OPERATIVO);
        dto.setCriticidad(Criticidad.MEDIA);
        dto.setFechaCompra(compra);
        dto.setFechaFinGarantia(finGarantia);
        return dto;
    }
}
