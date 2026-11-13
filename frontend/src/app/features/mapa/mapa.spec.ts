import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { MapaComponent } from './mapa';
import { AuthService } from '../../core/services/auth.service';
import { SolicitacaoService } from '../../core/services/solicitacao.service';
import { Solicitacao } from '../../core/models/solicitacao.model';

const leaflet = vi.hoisted(() => {
  const mapa = {
    setView: vi.fn(),
    fitBounds: vi.fn(),
    remove: vi.fn(),
  };
  mapa.setView.mockReturnValue(mapa);
  const camada = { addTo: vi.fn(), clearLayers: vi.fn() };
  camada.addTo.mockReturnValue(camada);
  const marcador = { bindPopup: vi.fn(), addTo: vi.fn() };
  marcador.bindPopup.mockReturnValue(marcador);
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
  let admin = false;

  const solicitacao = (status: Solicitacao['status'], extra: Partial<Solicitacao> = {}): Solicitacao => ({
    id: `id-${status}`,
    protocolo: `DEN-2026-${status}`,
    status,
    prioridade: 'ALTA',
    dataAbertura: '2026-01-01T00:00:00',
    prazoAlvo: null,
    dataEncerramento: null,
    descricao: 'Buraco na rua',
    localizacao: {
      logradouro: null,
      bairro: 'Centro',
      cep: null,
      referencia: null,
      latitude: -23.4,
      longitude: -51.9,
    },
    anonimo: false,
    nomeContato: null,
    emailContato: null,
    categoria: { id: 'c1', nome: 'Via' },
    departamento: null,
    atendente: null,
    ...extra,
  });

  const montar = async () => {
    TestBed.configureTestingModule({
      imports: [MapaComponent],
      providers: [
        provideRouter([]),
        { provide: SolicitacaoService, useValue: { listar, listarPublicas } },
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

  beforeEach(() => {
    admin = false;
    vi.clearAllMocks();
    listar.mockReturnValue(of([]));
    listarPublicas.mockReturnValue(of([solicitacao('ABERTO'), solicitacao('TRIAGEM')]));
  });

  it('deve exibir a barra superior com o título e o link para a home', async () => {
    const { el } = await montar();

    expect(el.querySelector('.top-bar__title')?.textContent?.trim()).toBe('Mapa de solicitações');
    expect(el.querySelector('.top-bar__logo')?.getAttribute('href')).toBe('/home');
    expect(el.querySelector('.page-content .mapa-container')).not.toBeNull();
  });

  it('deve inicializar o mapa com a camada de tiles', async () => {
    const { el } = await montar();

    expect(leaflet.map).toHaveBeenCalledWith(el.querySelector('.mapa-container'));
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

  it('deve criar um marcador por solicitação com coordenadas e ajustar o enquadramento', async () => {
    listarPublicas.mockReturnValue(of([
      solicitacao('ABERTO'),
      solicitacao('TRIAGEM', {
        localizacao: { logradouro: null, bairro: 'Centro', cep: null, referencia: null, latitude: null, longitude: null },
      }),
    ]));

    const { el } = await montar();

    expect(leaflet.marker).toHaveBeenCalledTimes(1);
    expect(leaflet.marker).toHaveBeenCalledWith([-23.4, -51.9], expect.anything());
    expect(leaflet.mapa.fitBounds).toHaveBeenCalled();
    expect(el.querySelector('.mapa-total')?.textContent).toContain('1 solicitação');
  });

  it('solicitação sem localização não deve gerar marcador nem entrar no total', async () => {
    listarPublicas.mockReturnValue(of([solicitacao('ABERTO'), solicitacao('TRIAGEM', { localizacao: null })]));

    const { component, el } = await montar();

    expect(leaflet.marker).toHaveBeenCalledTimes(1);
    expect(component.visiveis().map(s => s.status)).toEqual(['ABERTO']);
    expect(el.querySelector('.mapa-total')?.textContent).toContain('1 solicitação');
  });

  it('sem nenhuma solicitação com coordenadas não deve ajustar o enquadramento', async () => {
    listarPublicas.mockReturnValue(of([solicitacao('ABERTO', { localizacao: null })]));

    await montar();

    expect(leaflet.marker).not.toHaveBeenCalled();
    expect(leaflet.mapa.fitBounds).not.toHaveBeenCalled();
  });

  it('filtrar por status deve redesenhar apenas os marcadores do status', async () => {
    const { component, el } = await montar();
    leaflet.marker.mockClear();

    component.filtrarPorStatus('TRIAGEM');

    expect(leaflet.camada.clearLayers).toHaveBeenCalled();
    expect(leaflet.marker).toHaveBeenCalledTimes(1);
    expect(component.visiveis().map(s => s.status)).toEqual(['TRIAGEM']);
    expect(el.querySelector('.mapa-total')).not.toBeNull();
  });

  it('o select de status deve acionar o filtro', async () => {
    const { component, fixture, el } = await montar();
    const select = el.querySelector('#filtro-status') as HTMLSelectElement;

    select.value = 'ABERTO';
    select.dispatchEvent(new Event('change'));
    fixture.detectChanges();

    expect(component.statusFiltro()).toBe('ABERTO');
    expect(el.querySelector('.mapa-total')?.textContent).toContain('1 solicitação');
  });

  it('limpar o filtro deve exibir todas as solicitações', async () => {
    const { component } = await montar();
    component.filtrarPorStatus('ABERTO');

    component.filtrarPorStatus('');

    expect(component.visiveis()).toHaveLength(2);
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
