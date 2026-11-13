import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { MapaComponent } from './mapa';
import { AuthService } from '../../core/services/auth.service';
import { SlaService } from '../../core/services/sla.service';
import { SolicitacaoService } from '../../core/services/solicitacao.service';
import { Solicitacao } from '../../core/models/solicitacao.model';

const leaflet = vi.hoisted(() => {
  const mapa = {
    setView: vi.fn(),
    remove: vi.fn(),
  };
  mapa.setView.mockReturnValue(mapa);
  const camada = { addTo: vi.fn(), clearLayers: vi.fn() };
  camada.addTo.mockReturnValue(camada);
  const marcador = { on: vi.fn(), addTo: vi.fn() };
  marcador.on.mockReturnValue(marcador);
  return {
    mapa,
    camada,
    marcador,
    map: vi.fn(() => mapa),
    layerGroup: vi.fn(() => camada),
    marker: vi.fn(() => marcador),
    divIcon: vi.fn(() => ({})),
    tileLayer: vi.fn(() => ({ addTo: vi.fn() })),
  };
});

vi.mock('leaflet', () => leaflet);

describe('MapaComponent', () => {
  const listar = vi.fn();
  const listarPublicas = vi.fn();
  const listarSla = vi.fn();
  let admin = false;

  const solicitacao = (protocolo: string, extra: Partial<Solicitacao> = {}): Solicitacao => ({
    id: `id-${protocolo}`,
    protocolo,
    status: 'TRIAGEM',
    prioridade: 'ALTA',
    dataAbertura: '2026-01-01T00:00:00',
    prazoAlvo: null,
    dataEncerramento: null,
    descricao: 'Buraco na rua',
    localizacao: {
      logradouro: 'Avenida Colombo, 5790',
      bairro: 'Zona 7',
      cep: '87020-900',
      referencia: 'Em frente à UEM',
      latitude: -23.4,
      longitude: -51.9,
    },
    anonimo: false,
    nomeContato: null,
    emailContato: null,
    categoria: { id: 'c1', nome: 'Manutenção Urbana' },
    departamento: { id: 'd1', nome: 'Prefeitura Municipal' },
    atendente: null,
    ...extra,
  });

  const montar = async () => {
    TestBed.configureTestingModule({
      imports: [MapaComponent],
      providers: [
        provideRouter([]),
        { provide: SolicitacaoService, useValue: { listar, listarPublicas } },
        { provide: SlaService, useValue: { listar: listarSla } },
        { provide: AuthService, useValue: { isAdmin: () => admin, isGestor: () => false, isAuthenticated: () => true } },
      ],
    });
    const fixture = TestBed.createComponent(MapaComponent);
    fixture.detectChanges();
    await vi.waitFor(() => expect(leaflet.layerGroup).toHaveBeenCalled());
    await fixture.whenStable();
    fixture.detectChanges();
    return { fixture, component: fixture.componentInstance, el: fixture.nativeElement as HTMLElement };
  };

  const protocolos = (component: MapaComponent) => component.visiveis().map(s => s.protocolo);

  const clicarNoPin = (indice: number) => {
    const handler = leaflet.marcador.on.mock.calls[indice][1] as () => void;
    handler();
  };

  beforeEach(() => {
    admin = false;
    vi.clearAllMocks();
    listar.mockReturnValue(of([]));
    listarPublicas.mockReturnValue(of([
      solicitacao('DEN-1', { status: 'TRIAGEM', prioridade: 'ALTA' }),
      solicitacao('DEN-2', { status: 'EM_EXECUCAO', prioridade: 'CRITICA' }),
    ]));
    listarSla.mockReturnValue(of([
      { id: 's1', prioridade: 'ALTA', prazoHoras: 24, descricao: 'Alta' },
      { id: 's2', prioridade: 'CRITICA', prazoHoras: 4, descricao: 'Crítica' },
    ]));
  });

  it('deve exibir a barra superior com o título e o link para a home', async () => {
    const { el } = await montar();

    expect(el.querySelector('.top-bar__title')?.textContent?.trim()).toBe('Mapa de solicitações');
    expect(el.querySelector('.top-bar__logo')?.getAttribute('href')).toBe('/home');
    expect(el.querySelector('.page-content .mapa-container')).not.toBeNull();
  });

  it('deve abrir o mapa centrado em Maringá com a camada de tiles', async () => {
    const { el } = await montar();

    expect(leaflet.map).toHaveBeenCalledWith(el.querySelector('.mapa-container'));
    expect(leaflet.mapa.setView).toHaveBeenCalledWith([-23.4205, -51.9331], 13);
    expect(leaflet.tileLayer).toHaveBeenCalled();
    expect(leaflet.layerGroup).toHaveBeenCalled();
  });

  it('visitante deve carregar as solicitações públicas', async () => {
    await montar();

    expect(listarPublicas).toHaveBeenCalled();
    expect(listar).not.toHaveBeenCalled();
  });

  it('admin deve carregar todas as solicitações', async () => {
    admin = true;

    await montar();

    expect(listar).toHaveBeenCalled();
    expect(listarPublicas).not.toHaveBeenCalled();
  });

  it('deve criar um marcador por solicitação com coordenadas', async () => {
    const { el } = await montar();

    expect(leaflet.marker).toHaveBeenCalledTimes(2);
    expect(leaflet.marker).toHaveBeenCalledWith([-23.4, -51.9], expect.objectContaining({ title: 'DEN-1' }));
    expect(el.querySelector('.mapa-total')?.textContent).toContain('2 solicitação');
  });

  it('solicitação sem localização ou sem coordenadas não deve gerar marcador nem entrar no total', async () => {
    listarPublicas.mockReturnValue(of([
      solicitacao('DEN-1'),
      solicitacao('DEN-2', { localizacao: null }),
      solicitacao('DEN-3', {
        localizacao: { logradouro: null, bairro: 'Centro', cep: null, referencia: null, latitude: null, longitude: null },
      }),
    ]));

    const { component, el } = await montar();

    expect(leaflet.marker).toHaveBeenCalledTimes(1);
    expect(protocolos(component)).toEqual(['DEN-1']);
    expect(el.querySelector('.mapa-total')?.textContent).toContain('1 solicitação');
  });

  it('solicitações encerradas não devem aparecer', async () => {
    admin = true;
    listar.mockReturnValue(of([solicitacao('DEN-1'), solicitacao('DEN-2', { status: 'ENCERRADO' })]));

    const { component } = await montar();

    expect(protocolos(component)).toEqual(['DEN-1']);
  });

  it('cidadão não deve ver solicitações ainda não triadas', async () => {
    listarPublicas.mockReturnValue(of([
      solicitacao('DEN-1'),
      solicitacao('DEN-2', { status: 'ABERTO', prioridade: null, departamento: null }),
      solicitacao('DEN-3', { departamento: null }),
    ]));

    const { component } = await montar();

    expect(protocolos(component)).toEqual(['DEN-1']);
  });

  it('admin deve ver também as solicitações não triadas', async () => {
    admin = true;
    listar.mockReturnValue(of([
      solicitacao('DEN-1'),
      solicitacao('DEN-2', { status: 'ABERTO', prioridade: null, departamento: null }),
    ]));

    const { component } = await montar();

    expect(protocolos(component)).toEqual(['DEN-1', 'DEN-2']);
  });

  it('o select de status não deve oferecer Encerrado', async () => {
    const { el } = await montar();

    const opcoes = Array.from(el.querySelectorAll('#filtro-status option')).map(o => o.getAttribute('value'));

    expect(opcoes).toEqual(['', 'ABERTO', 'TRIAGEM', 'EM_EXECUCAO', 'RESOLVIDO']);
  });

  it('o select de status deve filtrar e redesenhar os marcadores', async () => {
    const { component, fixture, el } = await montar();
    leaflet.marker.mockClear();
    const select = el.querySelector('#filtro-status') as HTMLSelectElement;

    select.value = 'EM_EXECUCAO';
    select.dispatchEvent(new Event('change'));
    fixture.detectChanges();

    expect(component.statusFiltro()).toBe('EM_EXECUCAO');
    expect(leaflet.camada.clearLayers).toHaveBeenCalled();
    expect(leaflet.marker).toHaveBeenCalledTimes(1);
    expect(protocolos(component)).toEqual(['DEN-2']);
    expect(el.querySelector('.mapa-total')?.textContent).toContain('1 solicitação');
  });

  it('limpar o filtro de status deve exibir todas as solicitações', async () => {
    const { component } = await montar();
    component.filtrarPorStatus('TRIAGEM');

    component.filtrarPorStatus('');

    expect(component.visiveis()).toHaveLength(2);
  });

  it('o botão de filtro deve abrir e fechar o painel de gravidade', async () => {
    const { fixture, el } = await montar();
    const fab = el.querySelector('.filtro-fab') as HTMLButtonElement;

    fab.click();
    fixture.detectChanges();
    expect(el.querySelector('.filtro-painel')).not.toBeNull();
    expect(fab.getAttribute('aria-expanded')).toBe('true');

    fab.click();
    fixture.detectChanges();
    expect(el.querySelector('.filtro-painel')).toBeNull();
  });

  it('escolher uma gravidade deve filtrar, fechar o painel e marcar o botão como filtrado', async () => {
    const { component, fixture, el } = await montar();
    component.toggleFiltro();
    fixture.detectChanges();
    leaflet.marker.mockClear();

    const chip = Array.from(el.querySelectorAll<HTMLButtonElement>('.filtro-chip')).find(c => c.textContent?.includes('Gravíssimas'));
    chip?.click();
    fixture.detectChanges();

    expect(component.gravidadeFiltro()).toBe('CRITICA');
    expect(protocolos(component)).toEqual(['DEN-2']);
    expect(leaflet.marker).toHaveBeenCalledTimes(1);
    expect(el.querySelector('.filtro-painel')).toBeNull();
    expect(el.querySelector('.filtro-fab')?.textContent).toContain('Filtrado');
  });

  it('escolher a mesma gravidade de novo ou Todas deve remover o filtro', async () => {
    const { component } = await montar();

    component.filtrarPorGravidade('ALTA');
    component.filtrarPorGravidade('ALTA');
    expect(component.gravidadeFiltro()).toBeNull();

    component.filtrarPorGravidade('ALTA');
    component.filtrarPorGravidade(null);
    expect(component.gravidadeFiltro()).toBeNull();
    expect(component.visiveis()).toHaveLength(2);
  });

  it('filtros de status e gravidade devem ser combinados', async () => {
    admin = true;
    listar.mockReturnValue(of([
      solicitacao('DEN-1', { status: 'TRIAGEM', prioridade: 'ALTA' }),
      solicitacao('DEN-2', { status: 'TRIAGEM', prioridade: 'MEDIA' }),
      solicitacao('DEN-3', { status: 'EM_EXECUCAO', prioridade: 'ALTA' }),
    ]));
    const { component } = await montar();

    component.filtrarPorStatus('TRIAGEM');
    component.filtrarPorGravidade('ALTA');

    expect(protocolos(component)).toEqual(['DEN-1']);
  });

  it('visitante não deve ver a opção Sem dados na legenda nem no filtro', async () => {
    const { component, fixture, el } = await montar();
    component.toggleFiltro();
    fixture.detectChanges();

    expect(el.querySelectorAll('.mapa-legenda__item')).toHaveLength(4);
    expect(el.querySelector('.mapa-legenda')?.textContent).not.toContain('Sem dados');
    expect(el.querySelector('.filtro-painel')?.textContent).not.toContain('Sem dados');
  });

  it('admin deve ver Sem dados e poder filtrar as solicitações sem prioridade', async () => {
    admin = true;
    listar.mockReturnValue(of([
      solicitacao('DEN-1'),
      solicitacao('DEN-2', { status: 'ABERTO', prioridade: null, departamento: null }),
    ]));
    const { component, fixture, el } = await montar();
    component.toggleFiltro();
    fixture.detectChanges();

    expect(el.querySelectorAll('.mapa-legenda__item')).toHaveLength(5);
    const chip = Array.from(el.querySelectorAll<HTMLButtonElement>('.filtro-chip')).find(c => c.textContent?.includes('Sem dados'));
    chip?.click();

    expect(protocolos(component)).toEqual(['DEN-2']);
  });

  it('clicar no pin deve abrir o modal com os detalhes da solicitação', async () => {
    const { fixture, el } = await montar();

    clicarNoPin(0);
    fixture.detectChanges();

    const modal = el.querySelector('.modal') as HTMLElement;
    expect(modal).not.toBeNull();
    expect(modal.querySelector('.modal__categoria')?.textContent).toContain('Manutenção Urbana');
    expect(modal.querySelector('.modal__protocolo')?.textContent).toContain('DEN-1 · Triagem');
    expect(modal.querySelector('.modal__gravidade')?.textContent?.trim()).toBe('Grave');
    expect(modal.querySelector('.modal__valor--prazo')?.textContent).toContain('24h');
    expect(modal.querySelector('.modal__valor--local')?.textContent).toContain('Avenida Colombo, 5790');
    expect(modal.querySelector('.modal__valor--local')?.textContent).toContain('Zona 7');
    expect(modal.querySelector('.modal__valor--local')?.textContent).toContain('CEP 87020-900');
    expect(modal.textContent).toContain('Buraco na rua');
    expect(modal.querySelector('.modal__valor--referencia')?.textContent).toContain('Em frente à UEM');
  });

  it('modal de solicitação sem prioridade e sem localização deve exibir padrões', async () => {
    admin = true;
    listar.mockReturnValue(of([solicitacao('DEN-1', { status: 'ABERTO', prioridade: null, departamento: null })]));
    const { component, fixture, el } = await montar();

    component.selecionar(solicitacao('DEN-9', { prioridade: null, localizacao: null }));
    fixture.detectChanges();

    const modal = el.querySelector('.modal') as HTMLElement;
    expect(modal.querySelector('.modal__gravidade')?.textContent?.trim()).toBe('Sem dados');
    expect(modal.querySelector('.modal__valor--prazo')).toBeNull();
    expect(modal.querySelector('.modal__valor--local')?.textContent?.trim()).toBe('—');
    expect(modal.querySelector('.modal__valor--referencia')).toBeNull();
  });

  it('prazo deve exibir traço quando não há SLA para a prioridade', async () => {
    listarSla.mockReturnValue(throwError(() => new Error('500')));

    const { component } = await montar();

    expect(component.slaConfigs()).toEqual([]);
    expect(component.prazoSla('ALTA')).toBe('—');
  });

  it('o modal deve fechar pelo botão, pelo fundo e pela tecla Esc, mas não ao clicar dentro', async () => {
    const { fixture, el } = await montar();
    const abrir = () => { clicarNoPin(0); fixture.detectChanges(); };

    abrir();
    (el.querySelector('.modal') as HTMLElement).click();
    fixture.detectChanges();
    expect(el.querySelector('.modal')).not.toBeNull();

    (el.querySelector('.modal__fechar') as HTMLButtonElement).click();
    fixture.detectChanges();
    expect(el.querySelector('.modal')).toBeNull();

    abrir();
    (el.querySelector('.modal-overlay') as HTMLElement).click();
    fixture.detectChanges();
    expect(el.querySelector('.modal')).toBeNull();

    abrir();
    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }));
    fixture.detectChanges();
    expect(el.querySelector('.modal')).toBeNull();
  });

  it('tempoInfo deve descrever há quanto tempo a solicitação está aberta', async () => {
    const { component } = await montar();
    const abertura = new Date('2026-11-10T10:00:00').getTime();
    const dia = 24 * 60 * 60 * 1000;
    const sol = solicitacao('DEN-1', { dataAbertura: '2026-11-10T10:00:00' });

    expect(component.tempoInfo(sol, abertura + 2 * 60 * 60 * 1000)).toBe('Aberta hoje');
    expect(component.tempoInfo(sol, abertura + dia)).toBe('Aberta há 1 dia');
    expect(component.tempoInfo(sol, abertura + 5 * dia)).toBe('Aberta há 5 dias');
    expect(component.tempoInfo({ ...sol, status: 'RESOLVIDO' }, abertura + 5 * dia)).toBe('Resolvida');
  });

  it('rótulos e cores devem ter padrão para valores desconhecidos', async () => {
    const { component } = await montar();

    expect(component.rotuloStatus('X')).toBe('X');
    expect(component.rotuloPrioridade('X')).toBe('X');
    expect(component.corDe('X')).toBe('#9ca3af');
    expect(component.corDe(null)).toBe('#9ca3af');
  });

  it('erro ao carregar deve exibir mensagem', async () => {
    listarPublicas.mockReturnValue(throwError(() => new Error('500')));

    const { el } = await montar();

    expect(el.querySelector('.error-message')?.textContent).toContain('Não foi possível carregar');
  });

  it('ao destruir deve remover o mapa', async () => {
    const { fixture } = await montar();

    fixture.destroy();

    expect(leaflet.mapa.remove).toHaveBeenCalled();
  });
});
