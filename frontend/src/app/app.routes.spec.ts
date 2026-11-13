import { Route } from '@angular/router';
import { routes } from './app.routes';

describe('rotas da aplicação', () => {
  const rota = (path: string) => routes.find(r => r.path === path) as Route;

  it('a raiz deve redirecionar para /home', () => {
    expect(rota('')).toMatchObject({ redirectTo: 'home', pathMatch: 'full' });
  });

  it('rota desconhecida deve redirecionar para /login', () => {
    expect(rota('**').redirectTo).toBe('login');
  });

  it.each(['login', 'cadastro', 'home', 'mapa', 'triagem'])('/%s deve carregar o componente sob demanda', async path => {
    const componente = await (rota(path).loadComponent as () => Promise<unknown>)();

    expect(componente).toBeTypeOf('function');
  });
});
