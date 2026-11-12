import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { AuthResponse } from '../models/auth.model';
import { environment } from '../../../environments/environment';

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;

  const resposta = (perfil: string): AuthResponse => ({
    token: 'jwt-123',
    email: 'user@gov',
    nome: 'Usuário',
    perfil,
  });

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('login deve chamar a API e guardar token e usuário', () => {
    service.login({ email: 'user@gov', senha: '123' }).subscribe();

    const req = http.expectOne(`${environment.apiUrl}/auth/login`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ email: 'user@gov', senha: '123' });
    req.flush(resposta('GESTOR'));

    expect(service.getToken()).toBe('jwt-123');
    expect(service.getCurrentUser()?.nome).toBe('Usuário');
    expect(service.isAuthenticated()).toBe(true);
  });

  it('logout deve remover token e usuário', () => {
    service.login({ email: 'user@gov', senha: '123' }).subscribe();
    http.expectOne(`${environment.apiUrl}/auth/login`).flush(resposta('CIDADAO'));

    service.logout();

    expect(service.getToken()).toBeNull();
    expect(service.getCurrentUser()).toBeNull();
    expect(service.isAuthenticated()).toBe(false);
  });

  it('cadastrar deve enviar POST para /usuarios/cadastrar', () => {
    const corpo = {
      nome: 'Maria',
      email: 'maria@gov',
      cpf: '52998224725',
      telefone: '41999999999',
      senha: 'Abcdef1!',
      perfil: 'CIDADAO',
    };
    service.cadastrar(corpo).subscribe();

    const req = http.expectOne(`${environment.apiUrl}/usuarios/cadastrar`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(corpo);
    req.flush(null);
  });

  it('sem login, não deve estar autenticado nem ter usuário', () => {
    expect(service.getToken()).toBeNull();
    expect(service.getCurrentUser()).toBeNull();
    expect(service.isAuthenticated()).toBe(false);
    expect(service.isAdmin()).toBe(false);
    expect(service.isGestor()).toBe(false);
  });

  it.each([
    ['GESTOR', true, true],
    ['ATENDENTE', true, false],
    ['CIDADAO', false, false],
  ])('perfil %s: isAdmin=%s e isGestor=%s', (perfil, admin, gestor) => {
    service.login({ email: 'user@gov', senha: '123' }).subscribe();
    http.expectOne(`${environment.apiUrl}/auth/login`).flush(resposta(perfil));

    expect(service.isAdmin()).toBe(admin);
    expect(service.isGestor()).toBe(gestor);
  });
});
