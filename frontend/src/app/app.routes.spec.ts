import { Route } from '@angular/router';
import { routes } from './app.routes';
import { authGuard } from './core/guards/auth.guard';
import { gestorGuard } from './core/guards/gestor.guard';

describe('rotas da aplicação', () => {
  const rota = (path: string) => routes.find(r => r.path === path) as Route;

  it('a raiz deve redirecionar para /home', () => {
    expect(rota('')).toMatchObject({ redirectTo: 'home', pathMatch: 'full' });
  });

  it('rota desconhecida deve redirecionar para /login', () => {
    expect(rota('**').redirectTo).toBe('login');
  });

  it.each(['dashboard', 'logs'])('/%s deve exigir autenticação e perfil gestor', path => {
    const guards = rota(path).canActivate;

    expect(guards).toEqual([authGuard, gestorGuard]);
  });

  it.each(['login', 'cadastro', 'home', 'mapa', 'triagem', 'dashboard', 'logs'])('/%s deve carregar o componente sob demanda', async path => {
    const componente = await (rota(path).loadComponent as () => Promise<unknown>)();

    expect(componente).toBeTypeOf('function');
  });
});
