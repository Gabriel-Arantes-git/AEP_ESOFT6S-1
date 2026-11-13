import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Subject, of, throwError } from 'rxjs';
import { TriagemComponent } from './triagem';
import { AuthService } from '../../core/services/auth.service';
import { DepartamentoService } from '../../core/services/departamento.service';
import { SolicitacaoService } from '../../core/services/solicitacao.service';
import { Solicitacao } from '../../core/models/solicitacao.model';

describe('TriagemComponent', () => {
  const listar = vi.fn();
  const moverStatus = vi.fn();
  const listarAtivos = vi.fn();
  let gestor = false;

  const solicitacao = (status: Solicitacao['status'], extra: Partial<Solicitacao> = {}): Solicitacao => ({
    id: `id-${status}`,
    protocolo: `DEN-2026-${status}`,
    status,
    prioridade: null,
    dataAbertura: '2026-01-01T00:00:00',
    prazoAlvo: null,
    dataEncerramento: null,
    descricao: 'Buraco na rua',
    localizacao: {
      logradouro: null,
      bairro: 'Centro',
      cep: null,
      referencia: null,
      latitude: null,
      longitude: null,
    },
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
      imports: [TriagemComponent],
      providers: [
        provideRouter([]),
        { provide: SolicitacaoService, useValue: { listar, moverStatus } },
        { provide: DepartamentoService, useValue: { listarAtivos } },
        {
          provide: AuthService,
          useValue: { isGestor: () => gestor, isAdmin: () => true, isAuthenticated: () => true },
        },
      ],
    });
    const fixture = TestBed.createComponent(TriagemComponent);
    fixture.detectChanges();
    return {
      fixture,
      component: fixture.componentInstance,
      el: fixture.nativeElement as HTMLElement,
      botao: () => fixture.nativeElement.querySelector('.btn-mover') as HTMLButtonElement,
    };
  };

  beforeEach(() => {
    gestor = false;
    listar.mockReset().mockReturnValue(of([solicitacao('ABERTO'), solicitacao('RESOLVIDO'), solicitacao('ENCERRADO')]));
    moverStatus.mockReset();
    listarAtivos.mockReset().mockReturnValue(of([{ id: 'd1', nome: 'Obras' }]));
  });

  it('deve exibir a barra superior com o título e o link para a home', () => {
    const { el } = montar();

    expect(el.querySelector('.top-bar__title')?.textContent?.trim()).toBe('Fila de triagem');
    expect(el.querySelector('.top-bar__logo')?.getAttribute('href')).toBe('/home');
    expect(el.querySelector('.page-content .triagem-lista')).not.toBeNull();
  });

  it('deve listar a fila sem as solicitações encerradas', () => {
    const { el } = montar();

    expect(el.querySelectorAll('.triagem-item')).toHaveLength(2);
  });

  it('deve exibir categoria e bairro da localização', () => {
    const { el } = montar();

    expect(el.querySelector('.triagem-item__categoria')?.textContent?.trim()).toBe('Via · Centro');
  });

  it('sem localização deve exibir só a categoria', () => {
    listar.mockReturnValue(of([solicitacao('ABERTO', { localizacao: null })]));

    const { el } = montar();

    expect(el.querySelector('.triagem-item__categoria')?.textContent?.trim()).toBe('Via');
  });

  it('ABERTO exige prioridade, departamento e comentário antes de mover para TRIAGEM', () => {
    const { component, fixture, botao } = montar();

    component.selecionar(solicitacao('ABERTO'));
    fixture.detectChanges();
    expect(botao().textContent).toContain('Triagem');
    expect(botao().disabled).toBe(true);

    component.form.patchValue({ prioridade: 'ALTA', departamentoId: 'd1' });
    fixture.detectChanges();
    expect(botao().disabled).toBe(true);

    component.form.patchValue({ comentario: 'Triado' });
    fixture.detectChanges();
    expect(botao().disabled).toBe(false);
  });

  it('mover deve enviar o próximo status e recarregar a fila', () => {
    moverStatus.mockReturnValue(of(undefined));
    const { component } = montar();

    component.selecionar(solicitacao('ABERTO'));
    component.form.setValue({ prioridade: 'ALTA', departamentoId: 'd1', comentario: 'Triado' });
    component.mover();

    expect(moverStatus).toHaveBeenCalledWith('id-ABERTO', {
      novoStatus: 'TRIAGEM',
      comentario: 'Triado',
      prioridade: 'ALTA',
      departamentoId: 'd1',
    });
    expect(listar).toHaveBeenCalledTimes(2);
    expect(component.selecionada()).toBeNull();
    expect(component.sucesso()).toContain('Triagem');
  });

  it('fora da triagem só o comentário é obrigatório', () => {
    moverStatus.mockReturnValue(of(undefined));
    const { component } = montar();

    component.selecionar(solicitacao('TRIAGEM', { prioridade: 'ALTA', departamento: { id: 'd1', nome: 'Obras' } }));
    expect(component.podeMover()).toBe(false);

    component.form.patchValue({ comentario: 'Em execução' });
    component.mover();

    expect(moverStatus.mock.calls[0][1].novoStatus).toBe('EM_EXECUCAO');
  });

  it('sem comentário não deve chamar a API', () => {
    const { component } = montar();

    component.selecionar(solicitacao('TRIAGEM'));
    component.mover();

    expect(moverStatus).not.toHaveBeenCalled();
  });

  it('atendente não pode encerrar', () => {
    const { component, fixture, el, botao } = montar();

    component.selecionar(solicitacao('RESOLVIDO'));
    component.form.patchValue({ comentario: 'Fechar' });
    fixture.detectChanges();
    component.mover();

    expect(botao().disabled).toBe(true);
    expect(el.textContent).toContain('Apenas o gestor pode encerrar');
    expect(moverStatus).not.toHaveBeenCalled();
  });

  it('gestor pode encerrar', () => {
    gestor = true;
    moverStatus.mockReturnValue(of(undefined));
    const { component } = montar();

    component.selecionar(solicitacao('RESOLVIDO'));
    component.form.patchValue({ comentario: 'Fechar' });
    component.mover();

    expect(moverStatus.mock.calls[0][1].novoStatus).toBe('ENCERRADO');
  });

  it('erro da API deve exibir mensagem e liberar o botão', () => {
    moverStatus.mockReturnValue(throwError(() => new Error('409')));
    const { component, fixture, el, botao } = montar();

    component.selecionar(solicitacao('TRIAGEM'));
    component.form.patchValue({ comentario: 'x' });
    component.mover();
    fixture.detectChanges();

    expect(el.querySelector('.error-message')?.textContent).toContain('Não foi possível mover');
    expect(botao().disabled).toBe(false);
  });

  it('durante a requisição deve desabilitar o botão', () => {
    moverStatus.mockReturnValue(new Subject());
    const { component, fixture, botao } = montar();

    component.selecionar(solicitacao('TRIAGEM'));
    component.form.patchValue({ comentario: 'x' });
    component.mover();
    fixture.detectChanges();

    expect(botao().disabled).toBe(true);
    expect(botao().textContent).toContain('Salvando...');
  });
});
