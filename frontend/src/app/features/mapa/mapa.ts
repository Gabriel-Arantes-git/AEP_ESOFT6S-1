import { AfterViewInit, Component, ElementRef, OnDestroy, OnInit, computed, inject, signal, viewChild } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { SolicitacaoService } from '../../core/services/solicitacao.service';
import { Solicitacao } from '../../core/models/solicitacao.model';
import {
  COR_PRIORIDADE,
  FILTRO_SEM_DADOS,
  PRIORIDADE_LABEL,
  STATUS_OPCOES
} from '../../core/constants/solicitacao.constants';
import { adicionarTileLayer, iconePin } from '../../core/utils/leaflet.utils';
import { BottomNavComponent } from '../../shared/components/bottom-nav/bottom-nav';

type Leaflet = typeof import('leaflet');

const CENTRO_PADRAO: [number, number] = [-14.235, -51.925];
const ZOOM_PADRAO = 4;

@Component({
  selector: 'app-mapa',
  standalone: true,
  imports: [BottomNavComponent],
  templateUrl: './mapa.html',
  styleUrl: './mapa.scss'
})
export class MapaComponent implements OnInit, AfterViewInit, OnDestroy {
  private readonly authService = inject(AuthService);
  private readonly solicitacaoService = inject(SolicitacaoService);
  private readonly container = viewChild.required<ElementRef<HTMLDivElement>>('mapa');

  private leaflet: Leaflet | null = null;
  private map: import('leaflet').Map | null = null;
  private camada: import('leaflet').LayerGroup | null = null;

  readonly statusOpcoes = STATUS_OPCOES;

  solicitacoes = signal<Solicitacao[]>([]);
  statusFiltro = signal('');
  error = signal('');

  visiveis = computed(() =>
    this.solicitacoes().filter(s =>
      s.latitude !== null &&
      s.longitude !== null &&
      (!this.statusFiltro() || s.status === this.statusFiltro())
    )
  );

  ngOnInit(): void {
    const obs = this.authService.isAdmin()
      ? this.solicitacaoService.listar()
      : this.solicitacaoService.listarPublicas();

    obs.subscribe({
      next: solicitacoes => {
        this.solicitacoes.set(solicitacoes);
        this.atualizarMarcadores();
      },
      error: () => this.error.set('Não foi possível carregar as solicitações.')
    });
  }

  async ngAfterViewInit(): Promise<void> {
    this.leaflet = await import('leaflet');
    this.map = this.leaflet.map(this.container().nativeElement).setView(CENTRO_PADRAO, ZOOM_PADRAO);
    adicionarTileLayer(this.leaflet, this.map);
    this.camada = this.leaflet.layerGroup().addTo(this.map);
    this.atualizarMarcadores();
  }

  ngOnDestroy(): void {
    this.map?.remove();
  }

  filtrarPorStatus(status: string): void {
    this.statusFiltro.set(status);
    this.atualizarMarcadores();
  }

  private atualizarMarcadores(): void {
    const L = this.leaflet;
    const camada = this.camada;
    if (!L || !this.map || !camada) return;

    camada.clearLayers();
    const posicoes = this.visiveis().map(s => {
      const posicao: [number, number] = [s.latitude as number, s.longitude as number];
      L.marker(posicao, { icon: iconePin(L, this.corDe(s)) })
        .bindPopup(this.criarPopup(s))
        .addTo(camada);
      return posicao;
    });

    if (posicoes.length) this.map.fitBounds(posicoes, { padding: [32, 32], maxZoom: 16 });
  }

  private corDe(solicitacao: Solicitacao): string {
    return solicitacao.prioridade ? COR_PRIORIDADE[solicitacao.prioridade] : FILTRO_SEM_DADOS.cor;
  }

  private criarPopup(solicitacao: Solicitacao): HTMLElement {
    const status = STATUS_OPCOES.find(s => s.valor === solicitacao.status)?.label ?? solicitacao.status;
    const prioridade = solicitacao.prioridade ? PRIORIDADE_LABEL[solicitacao.prioridade] : FILTRO_SEM_DADOS.label;
    const popup = document.createElement('div');
    popup.textContent = `${solicitacao.protocolo} · ${solicitacao.categoria.nome} · ${status} · ${prioridade}`;
    return popup;
  }
}
