import { TestBed } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ListagemComponent } from './listagem';
import { AuthService } from '../../../core/services/auth.service';
import { SolicitacaoService } from '../../../core/services/solicitacao.service';
import { SlaService } from '../../../core/services/sla.service';
import { Movimentacao, Solicitacao, StatusSolicitacao, paraSolicitacaoView } from '../../../core/models/solicitacao.model';

const solicitacao = (protocolo: string, extra: Partial<Solicitacao> = {}): Solicitacao => ({
  id: `id-${protocolo}`,
  protocolo,
  status: 'ABERTO' as StatusSolicitacao,
  prioridade: null,
  dataAbertura: '2026-11-01T10:00:00',
  prazoAlvo: null,
  dataEncerramento: null,
  descricao: 'Lixo acumulado na calçada',
  bairro: 'Centro',
  logradouro: 'Rua XV de Novembro',
  referencia: 'Em frente à praça',
  latitude: -23.4205,
  longitude: -51.9331,
  cep: '87013-230',
  anonimo: false,
  nomeContato: null,
  emailContato: null,
  categoria: { id: 'cat-1', nome: 'Lixo Irregular' },
  departamento: null,
  atendente: null,
  ...extra,
});

const triada = (protocolo: string) => solicitacao(protocolo, {
  status: 'TRIAGEM',
  prioridade: 'ALTA',
  departamento: { id: 'dep-1', nome: 'Prefeitura Municipal' },
});

const movimentacoes: Movimentacao[] = [
  {
    id: 'mov-1',
    statusAnterior: null,
    statusNovo: 'ABERTO',
    comentario: 'Solicitação aberta.',
    responsavelNome: null,
    dataMovimentacao: '2026-11-01T10:00:00',
  },
  {
    id: 'mov-2',
    statusAnterior: 'ABERTO',
    statusNovo: 'TRIAGEM',
    comentario: 'Encaminhada para a prefeitura',
    responsavelNome: 'Atendente',
    dataMovimentacao: '2026-11-02T09:30:00',
  },
];

describe('ListagemComponent', () => {
  const listar = vi.fn();
  const listarMinhas = vi.fn();
  const listarAnonimas = vi.fn();
  const buscarPorProtocolo = vi.fn();
  const historico = vi.fn();

  const montar = (perfil: 'anonimo' | 'cidadao' | 'admin') => {
    TestBed.configureTestingModule({
      imports: [ListagemComponent],
      providers: [
        provideRouter([]),
        {
          provide: AuthService,
          useValue: {
            isAuthenticated: () => perfil !== 'anonimo',
            isAdmin: () => perfil === 'admin',
            isGestor: () => false,
            logout: vi.fn(),
          },
        },
        { provide: SolicitacaoService, useValue: { listar, listarMinhas, listarAnonimas, buscarPorProtocolo, historico } },
        { provide: SlaService, useValue: { listar: () => of([{ id: 'sla-1', prioridade: 'ALTA', prazoHoras: 24, descricao: '24h' }]) } },
      ],
    });
    const fixture = TestBed.createComponent(ListagemComponent);
    fixture.detectChanges();
    return { fixture, component: fixture.componentInstance, el: fixture.nativeElement as HTMLElement };
  };

  const digitarProtocolo = (el: HTMLElement, valor: string) => {
    const input = el.querySelector<HTMLInputElement>('.protocolo-input')!;
    input.value = valor;
    input.dispatchEvent(new Event('input'));
  };

  const erroHttp = (mensagem?: string) =>
    throwError(() => new HttpErrorResponse({ status: 400, error: mensagem ? { mensagem } : null }));

  beforeEach(() => {
    [listar, listarMinhas, listarAnonimas, buscarPorProtocolo, historico].forEach(fn => fn.mockReset());
    listar.mockReturnValue(of([]));
    listarMinhas.mockReturnValue(of([]));
    historico.mockReturnValue(of(movimentacoes));
  });

  it('anônimo deve ver apenas a consulta por protocolo, sem lista', () => {
    const { el } = montar('anonimo');

    expect(el.querySelector('.protocolo-input')).not.toBeNull();
    expect(el.querySelector('.lista')).toBeNull();
    expect(listar).not.toHaveBeenCalled();
    expect(listarMinhas).not.toHaveBeenCalled();
    expect(listarAnonimas).not.toHaveBeenCalled();
  });

  it('cidadão logado deve listar as próprias solicitações', () => {
    listarMinhas.mockReturnValue(of([solicitacao('DEN-2026-00001'), solicitacao('DEN-2026-00002')]));
    const { el } = montar('cidadao');

    expect(listarMinhas).toHaveBeenCalled();
    expect(listar).not.toHaveBeenCalled();
    expect(el.querySelectorAll('.card').length).toBe(2);
    expect(el.querySelector('.badge--analise')?.textContent).toContain('Em análise');
  });

  it('atendente ou gestor deve listar todas as solicitações', () => {
    listar.mockReturnValue(of([triada('DEN-2026-00003')]));
    const { el } = montar('admin');

    expect(listar).toHaveBeenCalled();
    expect(listarMinhas).not.toHaveBeenCalled();
    expect(el.querySelector('.card .badge')?.textContent).toContain('Triagem');
  });

  it('lista vazia deve mostrar o aviso de nenhuma denúncia', () => {
    const { el } = montar('cidadao');

    expect(el.querySelector('.lista-vazia')?.textContent).toContain('Nenhuma denúncia encontrada.');
  });

  it('consulta por protocolo deve normalizar o texto, abrir o modal e mostrar o histórico', () => {
    buscarPorProtocolo.mockReturnValue(of(triada('DEN-2026-00001')));
    const { component, fixture, el } = montar('anonimo');

    digitarProtocolo(el, '  den-2026-00001 ');
    fixture.detectChanges();
    el.querySelector<HTMLFormElement>('.protocolo-area')!.dispatchEvent(new Event('submit'));
    fixture.detectChanges();

    expect(buscarPorProtocolo).toHaveBeenCalledWith('DEN-2026-00001');
    expect(historico).toHaveBeenCalledWith('id-DEN-2026-00001');
    expect(component.modalAberta()?.protocolo).toBe('DEN-2026-00001');
    expect(component.consultandoProtocolo()).toBe(false);

    const itens = el.querySelectorAll('.historico__item');
    expect(itens.length).toBe(2);
    expect(itens[0].querySelector('.historico__status')?.textContent?.trim()).toBe('Aberto');
    expect(itens[0].querySelector('.historico__meta')?.textContent).toContain('—');
    expect(itens[1].querySelector('.historico__status')?.textContent?.trim()).toBe('Aberto → Triagem');
    expect(itens[1].querySelector('.historico__comentario')?.textContent).toContain('Encaminhada para a prefeitura');
    expect(itens[1].querySelector('.historico__meta')?.textContent).toContain('02/11/2026 09:30 · Atendente');
  });

  it('consulta com protocolo em branco não deve chamar a API', () => {
    const { component, el } = montar('anonimo');

    component.consultarProtocolo();

    expect(buscarPorProtocolo).not.toHaveBeenCalled();
    expect(el.querySelector<HTMLButtonElement>('.btn-consultar')?.disabled).toBe(true);
  });

  it('protocolo inexistente deve mostrar a mensagem do backend', () => {
    buscarPorProtocolo.mockReturnValue(erroHttp('Protocolo não encontrado: DEN-2026-99999'));
    const { component, fixture, el } = montar('anonimo');

    component.protocoloBusca.set('DEN-2026-99999');
    component.consultarProtocolo();
    fixture.detectChanges();

    expect(el.querySelector('.error-message')?.textContent).toContain('Protocolo não encontrado: DEN-2026-99999');
    expect(component.modalAberta()).toBeNull();
    expect(component.consultandoProtocolo()).toBe(false);
  });

  it('erro sem mensagem na consulta deve mostrar o texto padrão', () => {
    buscarPorProtocolo.mockReturnValue(erroHttp());
    const { component } = montar('anonimo');

    component.protocoloBusca.set('DEN-2026-00001');
    component.consultarProtocolo();

    expect(component.erroProtocolo()).toBe('Não foi possível consultar o protocolo.');
  });

  it('modal de denúncia ainda não triada deve mostrar que está em análise', () => {
    historico.mockReturnValue(of([]));
    const { component, fixture, el } = montar('cidadao');

    component.abrirModal(paraSolicitacaoView(solicitacao('DEN-2026-00001')));
    fixture.detectChanges();

    expect(el.querySelector('.modal__analise-titulo')?.textContent).toContain('Denúncia em análise');
    expect(el.querySelector('.modal')?.textContent).toContain('Nenhuma movimentação registrada.');
  });

  it('modal de denúncia triada deve mostrar andamento, prioridade, prazo e localização', () => {
    const { component, fixture, el } = montar('cidadao');

    component.abrirModal(paraSolicitacaoView(triada('DEN-2026-00001')));
    fixture.detectChanges();

    const modal = el.querySelector('.modal')!.textContent!;
    expect(el.querySelectorAll('.stepper__ponto--feito').length).toBe(2);
    expect(modal).toContain('Alta');
    expect(modal).toContain('24h');
    expect(modal).toContain('Prefeitura Municipal');
    expect(modal).toContain('Rua XV de Novembro');
    expect(modal).toContain('CEP 87013-230');
  });

  it('erro ao carregar o histórico deve encerrar o carregamento', () => {
    historico.mockReturnValue(throwError(() => new Error('falha')));
    const { component } = montar('cidadao');

    component.abrirModal(paraSolicitacaoView(solicitacao('DEN-2026-00001')));

    expect(component.carregandoHistorico()).toBe(false);
    expect(component.historico()).toEqual([]);
  });

  it('fechar o modal deve limpar a solicitação aberta', () => {
    const { component } = montar('cidadao');
    component.abrirModal(paraSolicitacaoView(solicitacao('DEN-2026-00001')));

    component.fecharModal();

    expect(component.modalAberta()).toBeNull();
  });

  it('busca deve filtrar por protocolo, categoria ou descrição', () => {
    listarMinhas.mockReturnValue(of([
      solicitacao('DEN-2026-00001'),
      solicitacao('DEN-2026-00002', { categoria: { id: 'cat-2', nome: 'Queimada' }, descricao: 'Fogo no terreno' }),
    ]));
    const { component, el } = montar('cidadao');
    const input = el.querySelector<HTMLInputElement>('.busca-input:not(.protocolo-input)')!;

    input.value = 'queimada';
    component.onBusca({ target: input } as unknown as Event);

    expect(component.filtradas().map(s => s.protocolo)).toEqual(['DEN-2026-00002']);
  });

  it('filtros devem separar em análise, por status e voltar para todos', () => {
    listarMinhas.mockReturnValue(of([solicitacao('DEN-2026-00001'), triada('DEN-2026-00002')]));
    const { component, fixture, el } = montar('cidadao');

    component.toggleFiltros();
    fixture.detectChanges();
    expect(el.querySelectorAll('.chip').length).toBe(6);

    component.setFiltro('EM_ANALISE');
    expect(component.filtradas().map(s => s.protocolo)).toEqual(['DEN-2026-00001']);

    component.setFiltro('TRIAGEM');
    expect(component.filtradas().map(s => s.protocolo)).toEqual(['DEN-2026-00002']);

    component.setFiltro('TRIAGEM');
    expect(component.filtradas().length).toBe(2);
  });

  it('paginação deve respeitar o tamanho da tela e navegar entre páginas', () => {
    listarMinhas.mockReturnValue(of(Array.from({ length: 5 }, (_, i) => solicitacao(`DEN-2026-0000${i + 1}`))));
    const { component } = montar('cidadao');

    component.larguraJanela.set(500);
    expect(component.totalPaginas()).toBe(2);
    expect(component.paginaItens().length).toBe(4);

    component.proximaPagina();
    expect(component.paginaCorrente()).toBe(2);
    expect(component.paginaItens().length).toBe(1);

    component.proximaPagina();
    expect(component.paginaCorrente()).toBe(2);

    component.paginaAnterior();
    component.paginaAnterior();
    expect(component.paginaCorrente()).toBe(1);

    component.irPara(2);
    window.innerWidth = 1400;
    component.onResize();
    expect(component.pageSize()).toBe(12);
    expect(component.paginaCorrente()).toBe(1);
  });

  it('rótulos devem ter valor padrão quando não houver dado', () => {
    const { component } = montar('cidadao');

    expect(component.formatarData(null)).toBe('—');
    expect(component.prioridadeLabel(null)).toBe('—');
    expect(component.prazoLabel('BAIXA')).toBe('—');
    expect(component.statusLabel('DESCONHECIDO')).toBe('DESCONHECIDO');
  });
});
