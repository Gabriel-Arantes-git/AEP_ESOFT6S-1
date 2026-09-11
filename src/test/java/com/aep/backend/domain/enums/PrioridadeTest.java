package com.aep.backend.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PrioridadeTest {

    @Test
    @DisplayName("Deve conter exatamente as quatro prioridades suportadas, da menor para a maior")
    void deveConterExatamenteAsQuatroPrioridadesSuportadasDaMenorParaMaior() {
        Prioridade[] prioridades = Prioridade.values();

        assertEquals(4, prioridades.length);
        assertEquals(Prioridade.BAIXA, prioridades[0]);
        assertEquals(Prioridade.MEDIA, prioridades[1]);
        assertEquals(Prioridade.ALTA, prioridades[2]);
        assertEquals(Prioridade.CRITICA, prioridades[3]);
    }

    @Test
    @DisplayName("Deve converter o nome textual para o enum correspondente")
    void deveConverterNomeTextualParaEnumCorrespondente() {
        assertEquals(Prioridade.CRITICA, Prioridade.valueOf("CRITICA"));
    }
}
