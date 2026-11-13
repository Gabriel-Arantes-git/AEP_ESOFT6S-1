import { Component, HostListener, OnInit, computed, inject, signal, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { SolicitacaoService } from '../../../core/services/solicitacao.service';
import { Movimentacao, SolicitacaoView, paraSolicitacaoView } from '../../../core/models/solicitacao.model';
import { FILTRO_USUARIO, formatarPrazo, PRIORIDADES, STATUS_FLUXO, STATUS_OPCOES } from '../../../core/constants/solicitacao.constants';
import { SlaService } from '../../../core/services/sla.service';
import { SlaConfig } from '../../../core/models/sla.model';
import { BottomNavComponent } from '../../../shared/components/bottom-nav/bottom-nav';

const PAGE_SIZE_MOBILE = 4;
const BP_MD = 768;
const BP_XL = 1280;

@Component({
  selector: 'app-listagem',
  standalone: true,
  imports: [RouterLink, BottomNavComponent],
  templateUrl: './listagem.html',
  styleUrl: './listagem.scss',
})
export class ListagemComponent implements OnInit {
  private readonly solicitacaoService = inject(SolicitacaoService);
  private readonly auth = inject(AuthService);
  private readonly slaService = inject(SlaService);
  private readonly platformId = inject(PLATFORM_ID);

  readonly STATUS_OPCOES = STATUS_OPCOES;
  readonly slaConfigs = signal<SlaConfig[]>([]);

  readonly logado = signal(false);
  readonly isAdmin = signal(false);
  readonly filtroOpcoes = computed(() => this.isAdmin() ? STATUS_OPCOES : FILTRO_USUARIO);
  readonly solicitacoes = signal<SolicitacaoView[]>([]);
  readonly termoBusca = signal('');
  readonly filtroStatus = signal<string | null>(null);
  readonly filtrosAberto = signal(false);
  readonly paginaCorrente = signal(1);
  readonly modalAberta = signal<SolicitacaoView | null>(null);
  readonly historico = signal<Movimentacao[]>([]);
  readonly carregandoHistorico = signal(false);
  readonly protocoloBusca = signal('');
  readonly consultandoProtocolo = signal(false);
  readonly erroProtocolo = signal<string | null>(null);
  readonly larguraJanela = signal(0);

  readonly pageSize = computed(() => {
    const w = this.larguraJanela();
    if (w >= BP_XL) return PAGE_SIZE_MOBILE * 3;
    if (w >= BP_MD) return PAGE_SIZE_MOBILE * 2;
    return PAGE_SIZE_MOBILE;
  });

  readonly filtradas = computed(() => {
    const t = this.termoBusca().toLowerCase();
    const s = this.filtroStatus();
    return this.solicitacoes().filter(x => {
      const matchTermo = !t ||
        x.categoria.toLowerCase().includes(t) ||
        x.protocolo.toLowerCase().includes(t) ||
        x.descricao.toLowerCase().includes(t);
      const matchStatus = s === 'EM_ANALISE'
        ? !this.ehValidada(x)
        : !s || x.status === s;
      return matchTermo && matchStatus;
    });
  });

  readonly totalPaginas = computed(() => Math.max(1, Math.ceil(this.filtradas().length / this.pageSize())));
  readonly paginas = computed(() => Array.from({ length: this.totalPaginas() }, (_, i) => i + 1));
  readonly paginaItens = computed(() => {
    const tamanho = this.pageSize();
    const inicio = (this.paginaCorrente() - 1) * tamanho;
    return this.filtradas().slice(inicio, inicio + tamanho);
  });

  ngOnInit(): void {
    this.logado.set(this.auth.isAuthenticated());
    this.isAdmin.set(this.auth.isAdmin());
    this.carregarSolicitacoes();
    this.slaService.listar().subscribe({ next: configs => this.slaConfigs.set(configs) });
    if (isPlatformBrowser(this.platformId)) {
      this.larguraJanela.set(window.innerWidth);
    }
  }

  private carregarSolicitacoes(): void {
    if (!this.logado()) return;
    const obs = this.isAdmin()
      ? this.solicitacaoService.listar()
      : this.solicitacaoService.listarMinhas();
    obs.subscribe({ next: lista => this.solicitacoes.set(lista.map(paraSolicitacaoView)) });
  }

  @HostListener('window:resize')
  onResize(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    this.larguraJanela.set(window.innerWidth);
    if (this.paginaCorrente() > this.totalPaginas()) {
      this.paginaCorrente.set(this.totalPaginas());
    }
  }

  onBusca(event: Event): void {
    this.termoBusca.set((event.target as HTMLInputElement).value);
    this.paginaCorrente.set(1);
  }

  onProtocoloInput(event: Event): void {
    this.protocoloBusca.set((event.target as HTMLInputElement).value);
  }

  consultarProtocolo(): void {
    const protocolo = this.protocoloBusca().trim().toUpperCase();
    if (!protocolo) return;
    this.consultandoProtocolo.set(true);
    this.erroProtocolo.set(null);
    this.solicitacaoService.buscarPorProtocolo(protocolo).subscribe({
      next: sol => {
        this.consultandoProtocolo.set(false);
        this.abrirModal(paraSolicitacaoView(sol));
      },
      error: err => {
        this.consultandoProtocolo.set(false);
        this.erroProtocolo.set(err.error?.mensagem ?? 'Não foi possível consultar o protocolo.');
      },
    });
  }

  toggleFiltros(): void { this.filtrosAberto.update(v => !v); }

  setFiltro(status: string | null): void {
    this.filtroStatus.set(this.filtroStatus() === status ? null : status);
    this.paginaCorrente.set(1);
  }

  irPara(p: number): void { this.paginaCorrente.set(p); }
  paginaAnterior(): void { if (this.paginaCorrente() > 1) this.paginaCorrente.update(p => p - 1); }
  proximaPagina(): void { if (this.paginaCorrente() < this.totalPaginas()) this.paginaCorrente.update(p => p + 1); }

  abrirModal(sol: SolicitacaoView): void {
    this.modalAberta.set(sol);
    this.historico.set([]);
    this.carregandoHistorico.set(true);
    this.solicitacaoService.historico(sol.id).subscribe({
      next: movimentacoes => {
        this.historico.set(movimentacoes);
        this.carregandoHistorico.set(false);
      },
      error: () => this.carregandoHistorico.set(false),
    });
  }

  fecharModal(): void { this.modalAberta.set(null); }

  ehValidada(sol: SolicitacaoView): boolean {
    return sol.prioridade !== null && sol.departamento !== null;
  }

  statusLabel(status: string): string {
    return STATUS_OPCOES.find(s => s.valor === status)?.label ?? status;
  }

  prioridadeLabel(p: string | null): string {
    return PRIORIDADES.find(x => x.valor === p)?.label ?? '—';
  }

  prazoLabel(p: string | null): string {
    const config = this.slaConfigs().find(c => c.prioridade === p);
    return config ? formatarPrazo(config.prazoHoras) : '—';
  }

  statusIndice(status: string): number {
    return (STATUS_FLUXO as string[]).indexOf(status);
  }

  formatarData(iso: string | null): string {
    if (!iso) return '—';
    const d = new Date(iso);
    const p = (n: number) => n.toString().padStart(2, '0');
    return `${p(d.getDate())}/${p(d.getMonth() + 1)}/${d.getFullYear()} ${p(d.getHours())}:${p(d.getMinutes())}`;
  }
}
