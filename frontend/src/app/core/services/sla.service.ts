import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SlaConfig } from '../models/sla.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class SlaService {
  private readonly http = inject(HttpClient);

  listar(): Observable<SlaConfig[]> {
    return this.http.get<SlaConfig[]>(`${environment.apiUrl}/sla`);
  }
}
