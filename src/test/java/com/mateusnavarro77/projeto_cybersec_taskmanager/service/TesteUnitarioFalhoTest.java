package com.mateusnavarro77.projeto_cybersec_taskmanager.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class TesteUnitarioFalhoTest {

    @Test
    public void testeUnitarioFalho() {
        // Este teste falhará propositalmente
        int resultado = 2 + 2;

        assertTrue(resultado == 5, "Este teste falhou de propósito.");
    }

}
