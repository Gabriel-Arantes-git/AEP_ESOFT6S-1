import { Injectable, signal } from '@angular/core';

export interface RascunhoSolicitacao {
  categoria: string;
  categoriaId: string;
  descricao: string;
  pontoReferencia: string;
}

const RASCUNHO_INICIAL: RascunhoSolicitacao = {
  categoria: '',
  categoriaId: '',
  descricao: '',
  pontoReferencia: '',
};

@Injectable({ providedIn: 'root' })
export class SolicitacaoRascunhoService {
  private readonly RASCUNHO_KEY = 'aep_rascunho';

  private readonly _rascunho = signal<RascunhoSolicitacao>(this.carregar());

  readonly rascunho = this._rascunho.asReadonly();

  atualizar(dados: Partial<RascunhoSolicitacao>): void {
    this._rascunho.update(atual => ({ ...atual, ...dados }));
    localStorage.setItem(this.RASCUNHO_KEY, JSON.stringify(this._rascunho()));
  }

  limpar(): void {
    this._rascunho.set(RASCUNHO_INICIAL);
    localStorage.removeItem(this.RASCUNHO_KEY);
  }

  private carregar(): RascunhoSolicitacao {
    const salvo = localStorage.getItem(this.RASCUNHO_KEY);
    return salvo ? { ...RASCUNHO_INICIAL, ...JSON.parse(salvo) } : RASCUNHO_INICIAL;
  }
}
