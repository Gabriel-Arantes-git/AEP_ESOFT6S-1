import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LogAcao } from '../models/log.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class LogService {
  private readonly http = inject(HttpClient);

  listar(): Observable<LogAcao[]> {
    return this.http.get<LogAcao[]>(`${environment.apiUrl}/logs`);
  }
}
