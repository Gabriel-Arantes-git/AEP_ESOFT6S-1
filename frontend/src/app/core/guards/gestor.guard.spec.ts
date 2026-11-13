import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree, provideRouter } from '@angular/router';
import { gestorGuard } from './gestor.guard';
import { AuthService } from '../services/auth.service';

describe('gestorGuard', () => {
  const executar = () =>
    TestBed.runInInjectionContext(() =>
      gestorGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot)
    );

  const configurar = (gestor: boolean) => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: { isGestor: () => gestor } },
      ],
    });
  };

  it('deve liberar a rota quando o usuário é gestor', () => {
    configurar(true);

    const resultado = executar();

    expect(resultado).toBe(true);
  });

  it('deve redirecionar para /home quando o usuário não é gestor', () => {
    configurar(false);

    const resultado = executar();

    expect(resultado).toBeInstanceOf(UrlTree);
    expect(TestBed.inject(Router).serializeUrl(resultado as UrlTree)).toBe('/home');
  });
});
