import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DepartamentoDestino } from '../models/departamento.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class DepartamentoService {
  private readonly http = inject(HttpClient);

  listarAtivos(): Observable<DepartamentoDestino[]> {
    return this.http.get<DepartamentoDestino[]>(`${environment.apiUrl}/departamentos/ativos`);
  }
}
