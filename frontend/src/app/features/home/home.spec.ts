import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { Observable, of, throwError } from 'rxjs';
import { HomeComponent } from './home';
import { AuthService } from '../../core/services/auth.service';
import { SolicitacaoService } from '../../core/services/solicitacao.service';
import { Solicitacao, StatusSolicitacao } from '../../core/models/solicitacao.model';

const DIA = 24 * 60 * 60 * 1000;

const solicitacao = (status: StatusSolicitacao, encerradaHa: number | null): Solicitacao => ({
  id: `s-${Math.random()}`,
  protocolo: 'DEN-2026-00001',
  status,
  prioridade: null,
  dataAbertura: '2026-11-01T10:00:00',
  prazoAlvo: null,
  dataEncerramento: encerradaHa === null ? null : new Date(Date.now() - encerradaHa).toISOString(),
  descricao: 'Descrição',
  bairro: 'Centro',
  logradouro: null,
  referencia: null,
  latitude: null,
  longitude: null,
  cep: null,
  anonimo: false,
  nomeContato: null,
  emailContato: null,
  categoria: { id: 'c-1', nome: 'Lixo' },
  departamento: null,
  atendente: null,
});

describe('HomeComponent', () => {
  const listar = vi.fn();
  const listarPublicas = vi.fn();
  const logout = vi.fn();

  const montar = (opcoes: { usuario: { nome: string } | null; admin: boolean }) => {
    TestBed.configureTestingModule({
      imports: [HomeComponent],
      providers: [
        provideRouter([]),
        {
          provide: AuthService,
          useValue: {
            getCurrentUser: () => opcoes.usuario,
            isAdmin: () => opcoes.admin,
            isAuthenticated: () => !!opcoes.usuario,
            isGestor: () => false,
            logout,
          },
        },
        { provide: SolicitacaoService, useValue: { listar, listarPublicas } },
      ],
    });
    const fixture = TestBed.createComponent(HomeComponent);
    const navegar = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    fixture.detectChanges();
    return { fixture, component: fixture.componentInstance, el: fixture.nativeElement as HTMLElement, navegar };
  };

  const lista = (itens: Solicitacao[]): Observable<Solicitacao[]> => of(itens);

  beforeEach(() => {
    listar.mockReset();
    listarPublicas.mockReset();
    logout.mockReset();
  });

  it('deve saudar o usuário logado pelo nome', () => {
    listarPublicas.mockReturnValue(lista([]));
    const { el } = montar({ usuario: { nome: 'Maria' }, admin: false });

    expect(el.querySelector('.home-hero__nome')?.textContent).toContain('Maria');
  });

  it('sem usuário logado deve saudar como "Usuário"', () => {
    listarPublicas.mockReturnValue(lista([]));
    const { el } = montar({ usuario: null, admin: false });

    expect(el.querySelector('.home-hero__nome')?.textContent).toContain('Usuário');
  });

  it('equipe (ATENDENTE/GESTOR) deve carregar todas as solicitações', () => {
    listar.mockReturnValue(lista([]));
    montar({ usuario: { nome: 'Ana' }, admin: true });

    expect(listar).toHaveBeenCalledOnce();
    expect(listarPublicas).not.toHaveBeenCalled();
  });

  it('cidadão ou visitante deve carregar só as públicas', () => {
    listarPublicas.mockReturnValue(lista([]));
    montar({ usuario: null, admin: false });

    expect(listarPublicas).toHaveBeenCalledOnce();
    expect(listar).not.toHaveBeenCalled();
  });

  it('deve contar abertas e tratadas na última semana', () => {
    listarPublicas.mockReturnValue(
      lista([
        solicitacao('ABERTO', null),
        solicitacao('ABERTO', null),
        solicitacao('TRIAGEM', null),
        solicitacao('RESOLVIDO', 1 * DIA),
        solicitacao('ENCERRADO', 3 * DIA),
        solicitacao('ENCERRADO', 10 * DIA),
        solicitacao('RESOLVIDO', null),
      ])
    );
    const { component, fixture, el } = montar({ usuario: null, admin: false });
    fixture.detectChanges();

    expect(component.abertas()).toBe(2);
    expect(component.tratadasNaSemana()).toBe(2);
    const valores = Array.from(el.querySelectorAll('.stat-card__valor')).map(n => n.textContent?.trim());
    expect(valores).toEqual(['2', '2']);
  });

  it('falha ao carregar deve manter os contadores em 0', () => {
    listarPublicas.mockReturnValue(throwError(() => new Error('500')));
    const { component } = montar({ usuario: null, admin: false });

    expect(component.abertas()).toBe(0);
    expect(component.tratadasNaSemana()).toBe(0);
  });

  it('atalhos devem navegar para as rotas de solicitação, acompanhamento e mapa', () => {
    listarPublicas.mockReturnValue(lista([]));
    const { el, navegar } = montar({ usuario: null, admin: false });

    (el.querySelector('.btn-nova') as HTMLButtonElement).click();
    const cartoes = el.querySelectorAll<HTMLButtonElement>('.action-card');
    cartoes[0].click();
    cartoes[1].click();

    expect(navegar.mock.calls.map(c => c[0])).toEqual([
      ['/solicitacao/nova'],
      ['/acompanhamento'],
      ['/mapa'],
    ]);
  });

  it('logout deve encerrar a sessão e voltar ao login', () => {
    listarPublicas.mockReturnValue(lista([]));
    const { component, navegar } = montar({ usuario: { nome: 'Maria' }, admin: false });

    component.logout();

    expect(logout).toHaveBeenCalledOnce();
    expect(navegar).toHaveBeenCalledWith(['/login']);
  });
});
