import { TestBed } from '@angular/core/testing';
import { SolicitacaoRascunhoService } from './solicitacao-rascunho.service';

describe('SolicitacaoRascunhoService', () => {
  let service: SolicitacaoRascunhoService;

  beforeEach(() => {
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
});
