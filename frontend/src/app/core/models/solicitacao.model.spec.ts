import { Solicitacao, paraSolicitacaoView } from './solicitacao.model';

describe('paraSolicitacaoView', () => {
  const base: Solicitacao = {
    id: 'sol-1',
    protocolo: 'DEN-2026-00001',
    status: 'TRIAGEM',
    prioridade: 'ALTA',
    dataAbertura: '2026-11-01T10:00:00',
    prazoAlvo: '2026-11-02T10:00:00',
    dataEncerramento: null,
    descricao: 'Queimada em terreno baldio',
    localizacao: {
      logradouro: 'Rua A',
      bairro: 'Centro',
      cep: '80000-000',
      referencia: 'Perto da praça',
      latitude: -25.4,
      longitude: -49.2,
    },
    anonimo: false,
    nomeContato: null,
    emailContato: null,
    categoria: { id: 'cat-1', nome: 'Queimada' },
    departamento: { id: 'dep-1', nome: 'SEMA' },
    atendente: { id: 'u-1', nome: 'Ana' },
  };

  it('deve achatar categoria, departamento e localização', () => {
    const view = paraSolicitacaoView(base);

    expect(view).toMatchObject({
      id: 'sol-1',
      protocolo: 'DEN-2026-00001',
      categoria: 'Queimada',
      departamento: 'SEMA',
      departamentoId: 'dep-1',
      pontoReferencia: 'Perto da praça',
      lat: -25.4,
      lng: -49.2,
      cep: '80000-000',
      logradouro: 'Rua A',
    });
  });

  it('deve preencher padrões quando os campos opcionais são nulos', () => {
    const view = paraSolicitacaoView({
      ...base,
      departamento: null,
      localizacao: { logradouro: null, bairro: null, cep: null, referencia: null, latitude: null, longitude: null },
      dataEncerramento: '2026-11-03T09:00:00',
    });

    expect(view.departamento).toBeNull();
    expect(view.departamentoId).toBeNull();
    expect(view.pontoReferencia).toBe('');
    expect(view.lat).toBe(0);
    expect(view.lng).toBe(0);
    expect(view.cep).toBe('');
    expect(view.bairro).toBe('');
    expect(view.logradouro).toBe('');
    expect(view.dataAtualizacao).toBe('2026-11-03T09:00:00');
  });

  it('deve preencher padrões quando a solicitação não tem localização', () => {
    const view = paraSolicitacaoView({ ...base, localizacao: null });

    expect(view).toMatchObject({ pontoReferencia: '', lat: 0, lng: 0, cep: '', bairro: '', logradouro: '' });
  });
});
