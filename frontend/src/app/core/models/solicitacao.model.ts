export interface CategoriaInfo { id: string; nome: string; }
export interface DepartamentoInfo { id: string; nome: string; }
export interface UsuarioInfo { id: string; nome: string; }

export type StatusSolicitacao = 'ABERTO' | 'TRIAGEM' | 'EM_EXECUCAO' | 'RESOLVIDO' | 'ENCERRADO';
export type PrioridadeSolicitacao = 'BAIXA' | 'MEDIA' | 'ALTA' | 'CRITICA';

export interface Solicitacao {
  id: string;
  protocolo: string;
  status: StatusSolicitacao;
  prioridade: PrioridadeSolicitacao | null;
  dataAbertura: string;
  prazoAlvo: string | null;
  dataEncerramento: string | null;
  descricao: string;
  bairro: string;
  logradouro: string | null;
  referencia: string | null;
  latitude: number | null;
  longitude: number | null;
  cep: string | null;
  anonimo: boolean;
  nomeContato: string | null;
  emailContato: string | null;
  categoria: CategoriaInfo;
  departamento: DepartamentoInfo | null;
  atendente: UsuarioInfo | null;
}

export interface NovaSolicitacaoRequest {
  categoriaId: string;
  descricao: string;
  bairro: string;
  logradouro?: string | null;
  referencia?: string | null;
  anonimo: boolean;
  nomeContato?: string | null;
  emailContato?: string | null;
  latitude?: number | null;
  longitude?: number | null;
  cep?: string | null;
}

export interface MoverStatusRequest {
  novoStatus: string;
  comentario: string;
  prioridade?: string | null;
  departamentoId?: string | null;
}

export interface Movimentacao {
  id: string;
  statusAnterior: StatusSolicitacao | null;
  statusNovo: StatusSolicitacao;
  comentario: string;
  responsavelNome: string | null;
  dataMovimentacao: string;
}

export interface SolicitacaoView {
  id: string;
  protocolo: string;
  status: StatusSolicitacao;
  prioridade: PrioridadeSolicitacao | null;
  departamento: string | null;
  departamentoId: string | null;
  dataAbertura: string;
  dataAtualizacao: string | null;
  prazoAlvo: string | null;
  categoria: string;
  descricao: string;
  pontoReferencia: string;
  lat: number;
  lng: number;
  cep: string;
  bairro: string;
  logradouro: string;
}

export function paraSolicitacaoView(s: Solicitacao): SolicitacaoView {
  return {
    id: s.id,
    protocolo: s.protocolo,
    status: s.status,
    prioridade: s.prioridade,
    departamento: s.departamento?.nome ?? null,
    departamentoId: s.departamento?.id ?? null,
    dataAbertura: s.dataAbertura,
    dataAtualizacao: s.dataEncerramento,
    prazoAlvo: s.prazoAlvo,
    categoria: s.categoria.nome,
    descricao: s.descricao,
    pontoReferencia: s.referencia ?? '',
    lat: s.latitude ?? 0,
    lng: s.longitude ?? 0,
    cep: s.cep ?? '',
    bairro: s.bairro,
    logradouro: s.logradouro ?? '',
  };
}
