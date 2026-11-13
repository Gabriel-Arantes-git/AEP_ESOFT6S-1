import {
  COR_PRIORIDADE,
  FILTROS_MAPA,
  PRIORIDADES,
  STATUS_FLUXO,
  STATUS_OPCOES,
  formatarPrazo,
  proximoStatus,
} from './solicitacao.constants';

describe('solicitacao.constants', () => {
  it.each([
    ['ABERTO', 'TRIAGEM'],
    ['TRIAGEM', 'EM_EXECUCAO'],
    ['EM_EXECUCAO', 'RESOLVIDO'],
    ['RESOLVIDO', 'ENCERRADO'],
    ['ENCERRADO', null],
    ['INEXISTENTE', null],
  ])('proximoStatus(%s) deve retornar %s', (atual, esperado) => {
    expect(proximoStatus(atual)).toBe(esperado);
  });

  it('STATUS_FLUXO deve seguir a ordem ABERTO → ENCERRADO', () => {
    expect(STATUS_FLUXO).toEqual(['ABERTO', 'TRIAGEM', 'EM_EXECUCAO', 'RESOLVIDO', 'ENCERRADO']);
    expect(STATUS_OPCOES).toHaveLength(5);
  });

  it('toda prioridade deve ter cor e aparecer nos filtros do mapa', () => {
    for (const p of PRIORIDADES) {
      expect(COR_PRIORIDADE[p.valor]).toBeDefined();
    }
    expect(FILTROS_MAPA.map(f => f.prioridade)).toEqual(['CRITICA', 'ALTA', 'MEDIA', 'BAIXA']);
  });

  it.each([
    [4, '4h'],
    [24, '24h'],
    [48, '2 dias'],
    [72, '3 dias'],
    [168, '7 dias'],
    [30, '30h'],
  ])('formatarPrazo(%i) deve devolver %s', (horas, esperado) => {
    expect(formatarPrazo(horas)).toBe(esperado);
  });
});
