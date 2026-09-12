package com.aep.backend.domain.abstraction;

import com.aep.backend.domain.categoria.controller.CategoriaController;
import com.aep.backend.domain.categoria.entity.Categoria;
import com.aep.backend.domain.categoria.service.CategoriaService;
import com.aep.backend.infra.security.JwtTokenProvider;
import com.aep.backend.infra.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DefaultCrudControllerTest {

    @InjectMocks
    private CategoriaController categoriaController;

    private MockMvc mockMvc;

    @Mock
    private CategoriaService categoriaService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Mock
    private MongoMappingContext mongoMappingContext;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(categoriaController)
                .addFilters(new com.aep.backend.TestSecurityFilter())
                .build();
    }

    @Test
    @DisplayName("Deve retornar status 200 com a lista ao listar todos os registros")
    void deveRetornarStatus200ComListaAoListarTodosOsRegistros() throws Exception {
        Categoria categoria = new Categoria("Nome", "Descricao");
        categoria.setId("cat-1");
        org.mockito.Mockito.lenient().when(categoriaService.listarTodos()).thenReturn(List.of(categoria));

        mockMvc.perform(get("/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("cat-1"));
    }

    @Test
    @DisplayName("Deve retornar status 200 quando o id existir")
    void deveRetornarStatus200QuandoIdExistir() throws Exception {
        Categoria categoria = new Categoria("Nome", "Descricao");
        categoria.setId("cat-1");
        org.mockito.Mockito.lenient().when(categoriaService.buscarPorId("cat-1")).thenReturn(Optional.of(categoria));

        mockMvc.perform(get("/categorias/cat-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("cat-1"));
    }

    @Test
    @DisplayName("Deve retornar status 404 quando o id nao existir")
    void deveRetornarStatus404QuandoIdNaoExistir() throws Exception {
        org.mockito.Mockito.lenient().when(categoriaService.buscarPorId("inexistente")).thenReturn(Optional.empty());

        mockMvc.perform(get("/categorias/inexistente"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve retornar status 201 ao criar um registro valido")
    void deveRetornarStatus201AoCriarRegistroValido() throws Exception {
        Categoria salva = new Categoria("Nome", "Descricao");
        salva.setId("cat-1");
        org.mockito.Mockito.lenient().when(categoriaService.salvar(any(Categoria.class))).thenReturn(salva);

        mockMvc.perform(post("/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Nome\",\"descricao\":\"Descricao\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("cat-1"));
    }

    @Test
    @DisplayName("Deve retornar status 200 e sobrescrever o id da url ao atualizar")
    void deveRetornarStatus200ESobrescreverIdDaUrlAoAtualizar() throws Exception {
        Categoria atualizada = new Categoria("Nome Novo", "Descricao Nova");
        atualizada.setId("cat-1");
        org.mockito.Mockito.lenient().when(categoriaService.atualizar(any(Categoria.class))).thenReturn(atualizada);

        mockMvc.perform(put("/categorias/cat-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"outro-id\",\"nome\":\"Nome Novo\",\"descricao\":\"Descricao Nova\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("cat-1"));

        verify(categoriaService).atualizar(argThat(categoria -> categoria.getId().equals("cat-1")));
    }

    @Test
    @DisplayName("Deve retornar status 204 ao deletar um registro")
    void deveRetornarStatus204AoDeletarRegistro() throws Exception {
        mockMvc.perform(delete("/categorias/cat-1"))
                .andExpect(status().isNoContent());

        verify(categoriaService).deletar("cat-1");
    }
}
