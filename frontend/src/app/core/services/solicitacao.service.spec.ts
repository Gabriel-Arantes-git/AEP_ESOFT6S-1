import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { SolicitacaoService } from './solicitacao.service';
import { NovaSolicitacaoRequest } from '../models/solicitacao.model';
import { environment } from '../../../environments/environment';

describe('SolicitacaoService', () => {
  let service: SolicitacaoService;
  let http: HttpTestingController;
  const base = `${environment.apiUrl}/solicitacoes`;

  const nova: NovaSolicitacaoRequest = {
    categoriaId: 'cat-1',
    descricao: 'Lixo acumulado na calçada',
    localizacao: {
      logradouro: 'Rua XV de Novembro',
      bairro: 'Centro',
      cep: '87013-230',
      referencia: 'Em frente à praça',
      latitude: -23.4205,
      longitude: -51.9331,
    },
    anonimo: false,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(SolicitacaoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('criar deve enviar POST para /solicitacoes', () => {
    service.criar(nova).subscribe();

    const req = http.expectOne(base);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(nova);
    req.flush({});
  });

  it('criarAnonima deve enviar POST para /solicitacoes/anonima', () => {
    service.criarAnonima({ ...nova, anonimo: true }).subscribe();

    const req = http.expectOne(`${base}/anonima`);
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it.each([
    ['listar', base],
    ['listarMinhas', `${base}/minhas`],
    ['listarPublicas', `${base}/publicas`],
    ['listarAnonimas', `${base}/anonimas`],
  ] as const)('%s deve fazer GET em %s', (metodo, url) => {
    service[metodo]().subscribe();

    const req = http.expectOne(url);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('listarPorStatus deve fazer GET em /solicitacoes com o parametro status', () => {
    service.listarPorStatus('TRIAGEM').subscribe();

    const req = http.expectOne(r => r.url === base);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('status')).toBe('TRIAGEM');
    req.flush([]);
  });

  it('buscarPorProtocolo deve fazer GET em /protocolo/{protocolo}', () => {
    service.buscarPorProtocolo('DEN-2026-00001').subscribe();

    const req = http.expectOne(`${base}/protocolo/DEN-2026-00001`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('moverStatus deve enviar PATCH em /{id}/status com o corpo', () => {
    const corpo = { novoStatus: 'TRIAGEM', comentario: 'Em análise' };
    service.moverStatus('sol-1', corpo).subscribe();

    const req = http.expectOne(`${base}/sol-1/status`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual(corpo);
    req.flush(null);
  });

  it('historico deve fazer GET em /{id}/movimentacoes', () => {
    service.historico('sol-1').subscribe();

    const req = http.expectOne(`${base}/sol-1/movimentacoes`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('logs deve fazer GET em /{id}/logs', () => {
    service.logs('sol-1').subscribe();

    const req = http.expectOne(`${base}/sol-1/logs`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });
});
