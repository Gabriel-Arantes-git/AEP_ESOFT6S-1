import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LogAcao } from '../models/log.model';
import {
  MoverStatusRequest,
  Movimentacao,
  NovaSolicitacaoRequest,
  Solicitacao
} from '../models/solicitacao.model';

@Injectable({ providedIn: 'root' })
export class SolicitacaoService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/solicitacoes`;

  criar(req: NovaSolicitacaoRequest): Observable<Solicitacao> {
    return this.http.post<Solicitacao>(this.base, req);
  }

  criarAnonima(req: NovaSolicitacaoRequest): Observable<Solicitacao> {
    return this.http.post<Solicitacao>(`${this.base}/anonima`, req);
  }

  listar(): Observable<Solicitacao[]> {
    return this.http.get<Solicitacao[]>(this.base);
  }

  listarMinhas(): Observable<Solicitacao[]> {
    return this.http.get<Solicitacao[]>(`${this.base}/minhas`);
  }

  listarPublicas(): Observable<Solicitacao[]> {
    return this.http.get<Solicitacao[]>(`${this.base}/publicas`);
  }

  listarAnonimas(): Observable<Solicitacao[]> {
    return this.http.get<Solicitacao[]>(`${this.base}/anonimas`);
  }

  buscarPorProtocolo(protocolo: string): Observable<Solicitacao> {
    return this.http.get<Solicitacao>(`${this.base}/protocolo/${encodeURIComponent(protocolo)}`);
  }

  moverStatus(id: string, req: MoverStatusRequest): Observable<void> {
    return this.http.patch<void>(`${this.base}/${id}/status`, req);
  }

  historico(id: string): Observable<Movimentacao[]> {
    return this.http.get<Movimentacao[]>(`${this.base}/${id}/movimentacoes`);
  }

  logs(id: string): Observable<LogAcao[]> {
    return this.http.get<LogAcao[]>(`${this.base}/${id}/logs`);
  }
}
