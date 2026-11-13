import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { BottomNavComponent } from './bottom-nav';
import { AuthService } from '../../../core/services/auth.service';

describe('BottomNavComponent', () => {
  const logout = vi.fn();

  const montar = (opcoes: { logado: boolean; gestor: boolean; atendente?: boolean }) => {
    TestBed.configureTestingModule({
      imports: [BottomNavComponent],
      providers: [
        provideRouter([]),
        {
          provide: AuthService,
          useValue: {
            isAuthenticated: () => opcoes.logado,
            isGestor: () => opcoes.gestor,
            isAdmin: () => opcoes.gestor || !!opcoes.atendente,
            logout,
          },
        },
      ],
    });
    const fixture = TestBed.createComponent(BottomNavComponent);
    fixture.detectChanges();
    return { fixture, el: fixture.nativeElement as HTMLElement };
  };

  beforeEach(() => logout.mockReset());

  it('deve mostrar os links principais', () => {
    const { el } = montar({ logado: false, gestor: false });

    for (const rota of ['/home', '/solicitacao/nova', '/acompanhamento', '/mapa']) {
      expect(el.querySelector(`a[href="${rota}"]`)).not.toBeNull();
    }
  });

  it('visitante deve ver "Entrar" apontando para /login e não ver "Sair"', () => {
    const { el } = montar({ logado: false, gestor: false });

    expect(el.querySelector('a[href="/login"]')?.textContent).toContain('Entrar');
    expect(el.querySelector('button')).toBeNull();
  });

  it('usuário logado deve ver "Sair" e não ver "Entrar"', () => {
    const { el } = montar({ logado: true, gestor: false });

    expect(el.querySelector('button')?.textContent).toContain('Sair');
    expect(el.querySelector('a[href="/login"]')).toBeNull();
  });

  it('sair deve encerrar a sessão e navegar para /login', () => {
    const { el } = montar({ logado: true, gestor: false });
    const navegar = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);

    (el.querySelector('button') as HTMLButtonElement).click();

    expect(logout).toHaveBeenCalledOnce();
    expect(navegar).toHaveBeenCalledWith(['/login']);
  });

  it('link de dashboard aparece para GESTOR', () => {
    expect(montar({ logado: true, gestor: true }).el.querySelector('a[href="/dashboard"]')).not.toBeNull();
  });

  it('link de dashboard não aparece para outros perfis', () => {
    expect(montar({ logado: true, gestor: false }).el.querySelector('a[href="/dashboard"]')).toBeNull();
  });

  it('link de logs só aparece para GESTOR', () => {
    expect(montar({ logado: true, gestor: true }).el.querySelector('a[href="/logs"]')).not.toBeNull();
  });

  it('link de logs não aparece para outros perfis', () => {
    expect(montar({ logado: true, gestor: false }).el.querySelector('a[href="/logs"]')).toBeNull();
  });

  it('link de triagem aparece para ATENDENTE', () => {
    const { el } = montar({ logado: true, gestor: false, atendente: true });

    expect(el.querySelector('a[href="/triagem"]')?.textContent).toContain('Triagem');
    expect(el.querySelector('a[href="/dashboard"]')).toBeNull();
  });

  it('link de triagem aparece para GESTOR', () => {
    expect(montar({ logado: true, gestor: true }).el.querySelector('a[href="/triagem"]')).not.toBeNull();
  });

  it('link de triagem não aparece para cidadão nem visitante', () => {
    expect(montar({ logado: true, gestor: false }).el.querySelector('a[href="/triagem"]')).toBeNull();
    TestBed.resetTestingModule();
    expect(montar({ logado: false, gestor: false }).el.querySelector('a[href="/triagem"]')).toBeNull();
  });
});
