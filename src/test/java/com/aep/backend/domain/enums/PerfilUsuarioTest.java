package com.aep.backend.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PerfilUsuarioTest {

    @Test
    @DisplayName("Deve conter exatamente os tres perfis suportados, na ordem esperada")
    void deveConterExatamenteOsTresPerfisSuportadosNaOrdemEsperada() {
        PerfilUsuario[] perfis = PerfilUsuario.values();

        assertEquals(3, perfis.length);
        assertEquals(PerfilUsuario.CIDADAO, perfis[0]);
        assertEquals(PerfilUsuario.ATENDENTE, perfis[1]);
        assertEquals(PerfilUsuario.GESTOR, perfis[2]);
    }

    @Test
    @DisplayName("Deve converter o nome textual para o enum correspondente")
    void deveConverterNomeTextualParaEnumCorrespondente() {
        assertEquals(PerfilUsuario.GESTOR, PerfilUsuario.valueOf("GESTOR"));
    }
}
