import { AfterViewInit, Component, ElementRef, OnDestroy, OnInit, computed, inject, signal, viewChild } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { SlaService } from '../../core/services/sla.service';
import { SolicitacaoService } from '../../core/services/solicitacao.service';
import { Solicitacao } from '../../core/models/solicitacao.model';
import { SlaConfig } from '../../core/models/sla.model';
import {
  COR_PRIORIDADE,
  FILTRO_SEM_DADOS,
  FILTROS_MAPA,
  PRIORIDADE_LABEL,
  STATUS_OPCOES,
  formatarPrazo
} from '../../core/constants/solicitacao.constants';
import { adicionarTileLayer, iconePin } from '../../core/utils/leaflet.utils';
import { BottomNavComponent } from '../../shared/components/bottom-nav/bottom-nav';

type Leaflet = typeof import('leaflet');

const CENTRO_MARINGA: [number, number] = [-23.4205, -51.9331];
const ZOOM_PADRAO = 13;
const DIA_MS = 24 * 60 * 60 * 1000;

@Component({
  selector: 'app-mapa',
  standalone: true,
  imports: [RouterLink, BottomNavComponent],
  templateUrl: './mapa.html',
  styleUrl: './mapa.scss',
  host: { '(document:keydown.escape)': 'fechar()' }
})
export class MapaComponent implements OnInit, AfterViewInit, OnDestroy {
  private readonly authService = inject(AuthService);
  private readonly solicitacaoService = inject(SolicitacaoService);
  private readonly slaService = inject(SlaService);
  private readonly container = viewChild.required<ElementRef<HTMLDivElement>>('mapa');

  private leaflet: Leaflet | null = null;
  private map: import('leaflet').Map | null = null;
  private camada: import('leaflet').LayerGroup | null = null;

  readonly statusOpcoes = STATUS_OPCOES.filter(s => s.valor !== 'ENCERRADO');
  readonly filtrosGravidade = FILTROS_MAPA;
  readonly filtroSemDados = FILTRO_SEM_DADOS;
  readonly isAdmin = this.authService.isAdmin();

  solicitacoes = signal<Solicitacao[]>([]);
  slaConfigs = signal<SlaConfig[]>([]);
  statusFiltro = signal('');
  gravidadeFiltro = signal<string | null>(null);
  filtroAberto = signal(false);
  selecionada = signal<Solicitacao | null>(null);
  error = signal('');

  visiveis = computed(() =>
    this.solicitacoes().filter(s =>
      typeof s.localizacao?.latitude === 'number' &&
      typeof s.localizacao?.longitude === 'number' &&
      s.status !== 'ENCERRADO' &&
      (this.isAdmin || this.ehTriada(s)) &&
      (!this.statusFiltro() || s.status === this.statusFiltro()) &&
      this.atendeGravidade(s)
    )
  );

  ngOnInit(): void {
    const obs = this.isAdmin
      ? this.solicitacaoService.listar()
      : this.solicitacaoService.listarPublicas();

    obs.subscribe({
      next: solicitacoes => {
        this.solicitacoes.set(solicitacoes);
        this.atualizarMarcadores();
      },
      error: () => this.error.set('Não foi possível carregar as solicitações.')
    });

    this.slaService.listar().subscribe({
      next: configs => this.slaConfigs.set(configs),
      error: () => this.slaConfigs.set([])
    });
  }

  async ngAfterViewInit(): Promise<void> {
    this.leaflet = await import('leaflet');
    this.map = this.leaflet.map(this.container().nativeElement).setView(CENTRO_MARINGA, ZOOM_PADRAO);
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

  toggleFiltro(): void {
    this.filtroAberto.update(aberto => !aberto);
  }

  filtrarPorGravidade(prioridade: string | null): void {
    this.gravidadeFiltro.set(this.gravidadeFiltro() === prioridade ? null : prioridade);
    this.filtroAberto.set(false);
    this.atualizarMarcadores();
  }

  selecionar(solicitacao: Solicitacao): void {
    this.selecionada.set(solicitacao);
  }

  fechar(): void {
    this.selecionada.set(null);
  }

  rotuloStatus(status: string): string {
    return STATUS_OPCOES.find(s => s.valor === status)?.label ?? status;
  }

  rotuloPrioridade(prioridade: string | null): string {
    return prioridade ? (PRIORIDADE_LABEL[prioridade] ?? prioridade) : FILTRO_SEM_DADOS.label;
  }

  corDe(prioridade: string | null): string {
    return prioridade ? (COR_PRIORIDADE[prioridade] ?? FILTRO_SEM_DADOS.cor) : FILTRO_SEM_DADOS.cor;
  }

  prazoSla(prioridade: string | null): string {
    const config = this.slaConfigs().find(c => c.prioridade === prioridade);
    return config ? formatarPrazo(config.prazoHoras) : '—';
  }

  tempoInfo(solicitacao: Solicitacao, agora = Date.now()): string {
    if (solicitacao.status === 'RESOLVIDO') return 'Resolvida';
    const dias = Math.max(0, Math.floor((agora - new Date(solicitacao.dataAbertura).getTime()) / DIA_MS));
    if (dias === 0) return 'Aberta hoje';
    if (dias === 1) return 'Aberta há 1 dia';
    return `Aberta há ${dias} dias`;
  }

  private ehTriada(solicitacao: Solicitacao): boolean {
    return solicitacao.prioridade !== null && solicitacao.departamento !== null;
  }

  private atendeGravidade(solicitacao: Solicitacao): boolean {
    const filtro = this.gravidadeFiltro();
    if (filtro === null) return true;
    if (filtro === FILTRO_SEM_DADOS.prioridade) return solicitacao.prioridade === null;
    return solicitacao.prioridade === filtro;
  }

  private atualizarMarcadores(): void {
    const L = this.leaflet;
    const camada = this.camada;
    if (!L || !this.map || !camada) return;

    camada.clearLayers();
    for (const s of this.visiveis()) {
      const posicao: [number, number] = [s.localizacao?.latitude as number, s.localizacao?.longitude as number];
      L.marker(posicao, { icon: iconePin(L, this.corDe(s.prioridade)), title: s.protocolo })
        .on('click', () => this.selecionar(s))
        .addTo(camada);
    }
  }
}
