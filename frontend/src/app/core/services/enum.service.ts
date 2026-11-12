import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class EnumService {
  private readonly http = inject(HttpClient);

  listarStatusSolicitacao(): Observable<string[]> {
    return this.http.get<string[]>(`${environment.apiUrl}/enums/status-solicitacao`);
  }

  listarPrioridades(): Observable<string[]> {
    return this.http.get<string[]>(`${environment.apiUrl}/enums/prioridades`);
  }
}
