import { Solicitacao } from '../models/solicitacao.model';
import { PRIORIDADES, STATUS_OPCOES } from '../constants/solicitacao.constants';

export interface ResumoItem {
  valor: string;
  label: string;
  total: number;
}

export interface ResumoSla {
  noPrazo: number;
  atrasadas: number;
  semPrazo: number;
}

const STATUS_FINALIZADOS = ['RESOLVIDO', 'ENCERRADO'];

export function totaisPorStatus(solicitacoes: Solicitacao[]): ResumoItem[] {
  return STATUS_OPCOES.map(({ valor, label }) => ({
    valor,
    label,
    total: solicitacoes.filter(s => s.status === valor).length,
  }));
}

export function totaisPorPrioridade(solicitacoes: Solicitacao[]): ResumoItem[] {
  const itens = PRIORIDADES.map(({ valor, label }) => ({
    valor,
    label,
    total: solicitacoes.filter(s => s.prioridade === valor).length,
  }));
  const semPrioridade = solicitacoes.filter(s => !s.prioridade).length;
  return [...itens, { valor: 'SEM_PRIORIDADE', label: 'Sem prioridade', total: semPrioridade }];
}

export function resumirSla(solicitacoes: Solicitacao[], agora: Date): ResumoSla {
  const resumo: ResumoSla = { noPrazo: 0, atrasadas: 0, semPrazo: 0 };
  solicitacoes
    .filter(s => !STATUS_FINALIZADOS.includes(s.status))
    .forEach(s => {
      if (!s.prazoAlvo) resumo.semPrazo++;
      else if (new Date(s.prazoAlvo) < agora) resumo.atrasadas++;
      else resumo.noPrazo++;
    });
  return resumo;
}
