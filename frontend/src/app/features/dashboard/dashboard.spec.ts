import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { DashboardComponent } from './dashboard';
import { AuthService } from '../../core/services/auth.service';
import { SlaService } from '../../core/services/sla.service';
import { SolicitacaoService } from '../../core/services/solicitacao.service';
import { Solicitacao } from '../../core/models/solicitacao.model';

describe('DashboardComponent', () => {
  const listar = vi.fn();
  const listarSla = vi.fn();

  const solicitacao = (extra: Partial<Solicitacao>): Solicitacao => ({
    id: 'id',
    protocolo: 'DEN-2026-00001',
    status: 'ABERTO',
    prioridade: null,
    dataAbertura: '2026-11-01T10:00:00',
    prazoAlvo: null,
    dataEncerramento: null,
    descricao: 'Buraco',
    bairro: 'Centro',
    logradouro: null,
    referencia: null,
    latitude: null,
    longitude: null,
    cep: null,
    anonimo: false,
    nomeContato: null,
    emailContato: null,
    categoria: { id: 'c1', nome: 'Via' },
    departamento: null,
    atendente: null,
    ...extra,
  });

  const montar = () => {
    TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [
        provideRouter([]),
        { provide: SolicitacaoService, useValue: { listar } },
        { provide: SlaService, useValue: { listar: listarSla } },
        { provide: AuthService, useValue: { isGestor: () => true, isAuthenticated: () => true } },
      ],
    });
    const fixture = TestBed.createComponent(DashboardComponent);
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;
    return {
      el,
      cards: (grupo: string) =>
        Array.from(el.querySelectorAll(`[data-grupo="${grupo}"]`)).map(c => c.textContent),
    };
  };

  beforeEach(() => {
    listar.mockReset().mockReturnValue(of([
      solicitacao({ status: 'ABERTO' }),
      solicitacao({ status: 'TRIAGEM', prioridade: 'ALTA', prazoAlvo: '2999-01-01T00:00:00' }),
      solicitacao({ status: 'EM_EXECUCAO', prioridade: 'ALTA', prazoAlvo: '2000-01-01T00:00:00' }),
    ]));
    listarSla.mockReset().mockReturnValue(of([
      { id: 's1', prioridade: 'ALTA', prazoHoras: 48, descricao: 'Alta' },
    ]));
  });

  it('deve exibir o total de solicitações', () => {
    const { el } = montar();

    const total = el.querySelector('.dashboard-header__total')?.textContent;

    expect(total).toContain('3 solicitação');
  });

  it('deve exibir os totais por status', () => {
    const { cards } = montar();

    const resultado = cards('status');

    expect(resultado).toEqual(['1Aberto', '1Triagem', '1Execução', '0Resolvido', '0Encerrado']);
  });

  it('deve exibir os totais por prioridade incluindo sem prioridade', () => {
    const { cards } = montar();

    const resultado = cards('prioridade');

    expect(resultado).toContain('2Alta');
    expect(resultado).toContain('1Sem prioridade');
  });

  it('deve exibir o resumo de SLA e os prazos configurados', () => {
    const { cards, el } = montar();

    const resultado = cards('sla');

    expect(resultado).toEqual(['1No prazo', '1Atrasadas', '1Sem prazo']);
    expect(el.querySelector('.dashboard-sla__item')?.textContent).toContain('2 dias');
  });

  it('erro ao carregar deve exibir mensagem e ocultar os totais', () => {
    listar.mockReturnValue(throwError(() => new Error('500')));

    const { el } = montar();

    expect(el.querySelector('.error-message')?.textContent).toContain('Não foi possível carregar o dashboard.');
    expect(el.querySelector('.dashboard-card')).toBeNull();
  });
});

