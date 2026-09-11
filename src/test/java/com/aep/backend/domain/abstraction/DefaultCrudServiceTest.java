package com.aep.backend.domain.abstraction;

import com.aep.backend.domain.categoria.entity.Categoria;
import com.aep.backend.domain.categoria.repository.CategoriaRepository;
import com.aep.backend.domain.categoria.service.CategoriaService;
import com.aep.backend.domain.enums.Prioridade;
import com.aep.backend.domain.sla.entity.SlaConfig;
import com.aep.backend.domain.sla.repository.SlaRepository;
import com.aep.backend.domain.sla.service.SlaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultCrudServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private SlaRepository slaRepository;

    @Test
    @DisplayName("Deve inativar quando a entidade implementa Ativavel")
    void deveInativarQuandoEntidadeImplementaAtivavel() {
        CategoriaService categoriaService = new CategoriaService(categoriaRepository);
        Categoria categoria = new Categoria("Nome", "Descricao");
        categoria.setId("cat-1");
        when(categoriaRepository.findById("cat-1")).thenReturn(Optional.of(categoria));

        categoriaService.deletar("cat-1");

        assertFalse(categoria.isAtivo());
        verify(categoriaRepository).save(categoria);
        verify(categoriaRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve apagar quando a entidade nao implementa Ativavel")
    void deveApagarQuandoEntidadeNaoImplementaAtivavel() {
        SlaService slaService = new SlaService(slaRepository);
        SlaConfig sla = new SlaConfig(Prioridade.ALTA, 24, "Prazo alto");
        sla.setId("sla-1");
        when(slaRepository.findById("sla-1")).thenReturn(Optional.of(sla));

        slaService.deletar("sla-1");

        verify(slaRepository).deleteById("sla-1");
        verify(slaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Nao deve fazer nada ao deletar quando o id nao existe")
    void naoDeveFazerNadaAoDeletarQuandoIdNaoExiste() {
        CategoriaService categoriaService = new CategoriaService(categoriaRepository);
        when(categoriaRepository.findById("inexistente")).thenReturn(Optional.empty());

        categoriaService.deletar("inexistente");

        verify(categoriaRepository, never()).save(any());
        verify(categoriaRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve salvar quando chamado")
    void deveSalvarQuandoChamado() {
        CategoriaService categoriaService = new CategoriaService(categoriaRepository);
        Categoria categoria = new Categoria("Nome", "Descricao");
        when(categoriaRepository.save(categoria)).thenReturn(categoria);

        Categoria resultado = categoriaService.salvar(categoria);

        assertEquals(categoria, resultado);
        verify(categoriaRepository).save(categoria);
    }

    @Test
    @DisplayName("Deve buscar por id quando o registro existir")
    void deveBuscarPorIdQuandoRegistroExistir() {
        CategoriaService categoriaService = new CategoriaService(categoriaRepository);
        Categoria categoria = new Categoria("Nome", "Descricao");
        categoria.setId("cat-1");
        when(categoriaRepository.findById("cat-1")).thenReturn(Optional.of(categoria));

        Optional<Categoria> resultado = categoriaService.buscarPorId("cat-1");

        assertTrue(resultado.isPresent());
        assertEquals("cat-1", resultado.get().getId());
    }

    @Test
    @DisplayName("Deve retornar Optional vazio ao buscar por id quando o registro nao existir")
    void deveRetornarOptionalVazioAoBuscarPorIdQuandoRegistroNaoExistir() {
        CategoriaService categoriaService = new CategoriaService(categoriaRepository);
        when(categoriaRepository.findById("inexistente")).thenReturn(Optional.empty());

        Optional<Categoria> resultado = categoriaService.buscarPorId("inexistente");

        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Deve listar todos quando existirem registros")
    void deveListarTodosQuandoExistiremRegistros() {
        CategoriaService categoriaService = new CategoriaService(categoriaRepository);
        Categoria categoria = new Categoria("Nome", "Descricao");
        when(categoriaRepository.findAll()).thenReturn(List.of(categoria));

        List<Categoria> resultado = categoriaService.listarTodos();

        assertEquals(1, resultado.size());
    }

    @Test
    @DisplayName("Deve substituir todos os campos quando a atualizacao vier completa")
    void deveSubstituirTodosOsCamposQuandoAtualizacaoVierCompleta() {
        CategoriaService categoriaService = new CategoriaService(categoriaRepository);
        Categoria existente = new Categoria("Nome Antigo", "Descricao Antiga");
        existente.setId("cat-1");
        Categoria entrada = new Categoria("Nome Novo", "Descricao Nova");
        entrada.setId("cat-1");
        when(categoriaRepository.findById("cat-1")).thenReturn(Optional.of(existente));
        when(categoriaRepository.save(existente)).thenReturn(existente);

        Categoria resultado = categoriaService.atualizar(entrada);

        assertEquals("Nome Novo", resultado.getNome());
        assertEquals("Descricao Nova", resultado.getDescricao());
    }

    @Test
    @DisplayName("Deve manter valor antigo quando campo for omitido na atualizacao")
    void deveManterValorAntigoQuandoCampoForOmitidoNaAtualizacao() {
        CategoriaService categoriaService = new CategoriaService(categoriaRepository);
        Categoria existente = new Categoria("Nome Antigo", "Descricao Antiga");
        existente.setId("cat-1");
        Categoria entrada = new Categoria();
        entrada.setId("cat-1");
        entrada.setDescricao("Descricao Nova");
        when(categoriaRepository.findById("cat-1")).thenReturn(Optional.of(existente));
        when(categoriaRepository.save(existente)).thenReturn(existente);

        Categoria resultado = categoriaService.atualizar(entrada);

        assertEquals("Nome Antigo", resultado.getNome());
        assertEquals("Descricao Nova", resultado.getDescricao());
    }

    @Test
    @DisplayName("Nao deve atualizar quando o id nao existir")
    void naoDeveAtualizarQuandoIdNaoExistir() {
        CategoriaService categoriaService = new CategoriaService(categoriaRepository);
        Categoria entrada = new Categoria();
        entrada.setId("inexistente");
        when(categoriaRepository.findById("inexistente")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> categoriaService.atualizar(entrada));
    }
}
