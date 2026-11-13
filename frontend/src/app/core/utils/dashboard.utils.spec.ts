import { totaisPorPrioridade, totaisPorStatus, resumirSla } from './dashboard.utils';
import { Solicitacao } from '../models/solicitacao.model';

describe('dashboard.utils', () => {
  const solicitacao = (extra: Partial<Solicitacao>): Solicitacao => ({
    id: 'id',
    protocolo: 'DEN-2026-00001',
    status: 'ABERTO',
    prioridade: null,
    dataAbertura: '2026-11-01T10:00:00',
    prazoAlvo: null,
    dataEncerramento: null,
    descricao: 'Buraco',
    bairro: 'Centro',
    logradouro: null,
    referencia: null,
    latitude: null,
    longitude: null,
    cep: null,
    anonimo: false,
    nomeContato: null,
    emailContato: null,
    categoria: { id: 'c1', nome: 'Via' },
    departamento: null,
    atendente: null,
    ...extra,
  });

  it('totaisPorStatus deve contar cada status do fluxo, inclusive os zerados', () => {
    const lista = [solicitacao({ status: 'ABERTO' }), solicitacao({ status: 'ABERTO' }), solicitacao({ status: 'TRIAGEM' })];

    const resultado = totaisPorStatus(lista);

    expect(resultado.map(r => [r.valor, r.total])).toEqual([
      ['ABERTO', 2],
      ['TRIAGEM', 1],
      ['EM_EXECUCAO', 0],
      ['RESOLVIDO', 0],
      ['ENCERRADO', 0],
    ]);
  });

  it('totaisPorPrioridade deve contar por prioridade e separar as sem prioridade', () => {
    const lista = [
      solicitacao({ prioridade: 'ALTA' }),
      solicitacao({ prioridade: 'ALTA' }),
      solicitacao({ prioridade: 'BAIXA' }),
      solicitacao({ prioridade: null }),
    ];

    const resultado = totaisPorPrioridade(lista);

    expect(resultado.map(r => [r.valor, r.total])).toEqual([
      ['BAIXA', 1],
      ['MEDIA', 0],
      ['ALTA', 2],
      ['CRITICA', 0],
      ['SEM_PRIORIDADE', 1],
    ]);
  });

  it('resumirSla deve classificar apenas as solicitações em andamento', () => {
    const agora = new Date('2026-11-10T00:00:00');
    const lista = [
      solicitacao({ status: 'TRIAGEM', prazoAlvo: '2026-11-12T00:00:00' }),
      solicitacao({ status: 'EM_EXECUCAO', prazoAlvo: '2026-11-05T00:00:00' }),
      solicitacao({ status: 'ABERTO', prazoAlvo: null }),
      solicitacao({ status: 'RESOLVIDO', prazoAlvo: '2026-11-01T00:00:00' }),
      solicitacao({ status: 'ENCERRADO', prazoAlvo: '2026-11-01T00:00:00' }),
    ];

    const resultado = resumirSla(lista, agora);

    expect(resultado).toEqual({ noPrazo: 1, atrasadas: 1, semPrazo: 1 });
  });

  it('resumirSla com lista vazia deve zerar todos os contadores', () => {
    const resultado = resumirSla([], new Date());

    expect(resultado).toEqual({ noPrazo: 0, atrasadas: 0, semPrazo: 0 });
  });
});
