import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { LogsComponent } from './logs';
import { AuthService } from '../../core/services/auth.service';
import { LogService } from '../../core/services/log.service';
import { SolicitacaoService } from '../../core/services/solicitacao.service';
import { LogAcao } from '../../core/models/log.model';

describe('LogsComponent', () => {
  const listarSolicitacoes = vi.fn();
  const logsDaSolicitacao = vi.fn();
  const listarLogs = vi.fn();

  const log = (id: string, acao: string, extra: Partial<LogAcao> = {}): LogAcao => ({
    id,
    acao,
    entidade: 'Solicitacao',
    entidadeId: 'sol-1',
    detalhes: 'detalhe',
    usuarioNome: 'Gestor',
    dataAcao: '2026-11-01T10:00:00',
    ...extra,
  });

  const montar = () => {
    TestBed.configureTestingModule({
      imports: [LogsComponent],
      providers: [
        provideRouter([]),
        { provide: SolicitacaoService, useValue: { listar: listarSolicitacoes, logs: logsDaSolicitacao } },
        { provide: LogService, useValue: { listar: listarLogs } },
        { provide: AuthService, useValue: { isGestor: () => true, isAuthenticated: () => true } },
      ],
    });
    const fixture = TestBed.createComponent(LogsComponent);
    fixture.detectChanges();
    return {
      fixture,
      component: fixture.componentInstance,
      el: fixture.nativeElement as HTMLElement,
    };
  };

  beforeEach(() => {
    listarSolicitacoes.mockReset().mockReturnValue(of([{ id: 'sol-1', protocolo: 'DEN-2026-00001' }]));
    logsDaSolicitacao.mockReset().mockReturnValue(of([log('l3', 'MOVER_STATUS')]));
    listarLogs.mockReset().mockReturnValue(of([log('l1', 'CRIAR'), log('l2', 'LOGIN', { usuarioNome: null })]));
  });

  it('deve carregar todos os logs e as solicitações ao iniciar', () => {
    const { el } = montar();

    const itens = el.querySelectorAll('.logs-item');

    expect(listarLogs).toHaveBeenCalled();
    expect(itens).toHaveLength(2);
    expect(el.querySelectorAll('#filtro-solicitacao option')).toHaveLength(2);
  });

  it('log sem usuário deve ser exibido como Sistema', () => {
    const { el } = montar();

    const metas = Array.from(el.querySelectorAll('.logs-item__meta')).map(m => m.textContent);

    expect(metas[1]).toContain('Sistema');
  });

  it('selecionar uma solicitação deve carregar a trilha dela', () => {
    const { fixture, el } = montar();
    const select = el.querySelector('#filtro-solicitacao') as HTMLSelectElement;

    select.value = 'sol-1';
    select.dispatchEvent(new Event('change'));
    fixture.detectChanges();

    expect(logsDaSolicitacao).toHaveBeenCalledWith('sol-1');
    expect(el.querySelectorAll('.logs-item')).toHaveLength(1);
    expect(el.querySelector('.logs-item__acao')?.textContent).toContain('MOVER_STATUS');
  });

  it('voltar para todas deve recarregar os logs gerais', () => {
    const { fixture, component } = montar();
    component.filtrarPorSolicitacao('sol-1');

    component.filtrarPorSolicitacao('');
    fixture.detectChanges();

    expect(listarLogs).toHaveBeenCalledTimes(2);
    expect(component.logs()).toHaveLength(2);
  });

  it('sem logs deve exibir mensagem de vazio', () => {
    listarLogs.mockReturnValue(of([]));

    const { el } = montar();

    expect(el.querySelector('.logs-vazio')?.textContent).toContain('Nenhum log encontrado.');
  });

  it('erro ao carregar logs deve exibir mensagem e limpar a lista', () => {
    listarLogs.mockReturnValue(throwError(() => new Error('403')));

    const { el } = montar();

    expect(el.querySelector('.error-message')?.textContent).toContain('Não foi possível carregar os logs.');
    expect(el.querySelector('.logs-item')).toBeNull();
    expect(el.querySelector('.logs-vazio')).toBeNull();
  });
});
