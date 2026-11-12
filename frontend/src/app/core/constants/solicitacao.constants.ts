export const STATUS_OPCOES = [
  { valor: 'ABERTO',      label: 'Aberto' },
  { valor: 'TRIAGEM',     label: 'Triagem' },
  { valor: 'EM_EXECUCAO', label: 'Execução' },
  { valor: 'RESOLVIDO',   label: 'Resolvido' },
  { valor: 'ENCERRADO',   label: 'Encerrado' },
] as const;

export const STATUS_FLUXO = STATUS_OPCOES.map(s => s.valor);

export const FILTRO_USUARIO = [
  { valor: 'EM_ANALISE',  label: 'Em análise' },
  { valor: 'TRIAGEM',     label: 'Triagem' },
  { valor: 'EM_EXECUCAO', label: 'Execução' },
  { valor: 'RESOLVIDO',   label: 'Resolvido' },
  { valor: 'ENCERRADO',   label: 'Encerrado' },
] as const;

export const PRIORIDADES = [
  { valor: 'BAIXA',   label: 'Baixa' },
  { valor: 'MEDIA',   label: 'Média' },
  { valor: 'ALTA',    label: 'Alta' },
  { valor: 'CRITICA', label: 'Crítica' },
] as const;

export const COR_PRIORIDADE: Record<string, string> = {
  CRITICA: '#dc2626',
  ALTA:    '#ea580c',
  MEDIA:   '#d97706',
  BAIXA:   '#16a34a',
};

export const PRIORIDADE_LABEL: Record<string, string> = {
  CRITICA: 'Gravíssima',
  ALTA:    'Grave',
  MEDIA:   'Média',
  BAIXA:   'Leve',
};

export function formatarPrazo(horas: number): string {
  if (horas % 24 === 0 && horas > 24) return `${horas / 24} dias`;
  return `${horas}h`;
}

export const FILTROS_MAPA = [
  { label: 'Gravíssimas', prioridade: 'CRITICA', cor: COR_PRIORIDADE['CRITICA'] },
  { label: 'Graves',      prioridade: 'ALTA',    cor: COR_PRIORIDADE['ALTA'] },
  { label: 'Médias',      prioridade: 'MEDIA',   cor: COR_PRIORIDADE['MEDIA'] },
  { label: 'Leves',       prioridade: 'BAIXA',   cor: COR_PRIORIDADE['BAIXA'] },
] as const;

export const FILTRO_SEM_DADOS = { label: 'Sem dados', prioridade: 'SEM_DADOS', cor: '#9ca3af' } as const;
