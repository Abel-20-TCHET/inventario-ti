package com.example.colegio_ayacucho;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Verifica que el contexto de Spring arranca (con H2 en memoria, sin PostgreSQL).
 */
@SpringBootTest
@ActiveProfiles("test")
class ColegioAyacuchoApplicationTests {

    @Test
    void contextLoads() {
    }
}
