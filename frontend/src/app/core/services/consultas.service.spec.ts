import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Observable } from 'rxjs';
import { CategoriaService } from './categoria.service';
import { DepartamentoService } from './departamento.service';
import { EnumService } from './enum.service';
import { LogService } from './log.service';
import { SlaService } from './sla.service';
import { environment } from '../../../environments/environment';

describe('services de consulta (GET simples)', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  const casos: [string, () => Observable<unknown>, string][] = [
    ['CategoriaService.listarAtivas', () => TestBed.inject(CategoriaService).listarAtivas(), '/categorias/ativas'],
    ['DepartamentoService.listarAtivos', () => TestBed.inject(DepartamentoService).listarAtivos(), '/departamentos/ativos'],
    ['EnumService.listarStatusSolicitacao', () => TestBed.inject(EnumService).listarStatusSolicitacao(), '/enums/status-solicitacao'],
    ['EnumService.listarPrioridades', () => TestBed.inject(EnumService).listarPrioridades(), '/enums/prioridades'],
    ['LogService.listar', () => TestBed.inject(LogService).listar(), '/logs'],
    ['SlaService.listar', () => TestBed.inject(SlaService).listar(), '/sla'],
  ];

  it.each(casos)('%s deve fazer GET em %s e devolver o corpo', (_nome, chamar, caminho) => {
    const resultado: unknown[] = [];
    chamar().subscribe(r => resultado.push(r));

    const req = http.expectOne(`${environment.apiUrl}${caminho}`);
    expect(req.request.method).toBe('GET');
    req.flush([{ id: '1' }]);

    expect(resultado).toEqual([[{ id: '1' }]]);
  });
});
