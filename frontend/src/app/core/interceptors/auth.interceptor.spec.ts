import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from '../services/auth.service';

describe('authInterceptor', () => {
  let http: HttpClient;
  let controller: HttpTestingController;

  const configurar = (token: string | null) => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: { getToken: () => token } },
      ],
    });
    http = TestBed.inject(HttpClient);
    controller = TestBed.inject(HttpTestingController);
  };

  afterEach(() => controller.verify());

  it('deve anexar o cabeçalho Authorization quando há token', () => {
    configurar('jwt-abc');

    http.get('/api/qualquer').subscribe();

    const req = controller.expectOne('/api/qualquer');
    expect(req.request.headers.get('Authorization')).toBe('Bearer jwt-abc');
    req.flush({});
  });

  it('não deve anexar Authorization quando não há token', () => {
    configurar(null);

    http.get('/api/qualquer').subscribe();

    const req = controller.expectOne('/api/qualquer');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });
});
