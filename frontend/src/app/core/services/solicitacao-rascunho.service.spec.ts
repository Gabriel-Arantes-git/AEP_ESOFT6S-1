import { TestBed } from '@angular/core/testing';
import { SolicitacaoRascunhoService } from './solicitacao-rascunho.service';

describe('SolicitacaoRascunhoService', () => {
  let service: SolicitacaoRascunhoService;

  beforeEach(() => {
    localStorage.clear();
    service = TestBed.inject(SolicitacaoRascunhoService);
  });

  it('deve começar com o rascunho vazio', () => {
    expect(service.rascunho()).toEqual({
      categoria: '',
      categoriaId: '',
      descricao: '',
      pontoReferencia: '',
    });
  });

  it('atualizar deve mesclar só os campos informados', () => {
    service.atualizar({ categoria: 'Lixo', categoriaId: 'cat-1' });
    service.atualizar({ descricao: 'Descarte irregular' });

    expect(service.rascunho()).toEqual({
      categoria: 'Lixo',
      categoriaId: 'cat-1',
      descricao: 'Descarte irregular',
      pontoReferencia: '',
    });
  });

  it('limpar deve voltar ao estado inicial', () => {
    service.atualizar({ categoria: 'Lixo', descricao: 'x' });

    service.limpar();

    expect(service.rascunho().categoria).toBe('');
    expect(service.rascunho().descricao).toBe('');
  });

  it('atualizar deve salvar o rascunho no localStorage', () => {
    service.atualizar({ categoria: 'Lixo', categoriaId: 'cat-1' });

    expect(JSON.parse(localStorage.getItem('aep_rascunho')!)).toEqual({
      categoria: 'Lixo',
      categoriaId: 'cat-1',
      descricao: '',
      pontoReferencia: '',
    });
  });

  it('limpar deve remover o rascunho do localStorage', () => {
    service.atualizar({ categoria: 'Lixo' });

    service.limpar();

    expect(localStorage.getItem('aep_rascunho')).toBeNull();
  });

  it('deve restaurar o rascunho salvo no localStorage ao iniciar', () => {
    localStorage.setItem('aep_rascunho', JSON.stringify({ categoria: 'Queimada', descricao: 'Fogo no terreno' }));
    TestBed.resetTestingModule();

    const restaurado = TestBed.inject(SolicitacaoRascunhoService);

    expect(restaurado.rascunho()).toEqual({
      categoria: 'Queimada',
      categoriaId: '',
      descricao: 'Fogo no terreno',
      pontoReferencia: '',
    });
  });
});
