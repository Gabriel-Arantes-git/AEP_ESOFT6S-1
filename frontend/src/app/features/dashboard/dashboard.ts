import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { SlaService } from '../../core/services/sla.service';
import { SolicitacaoService } from '../../core/services/solicitacao.service';
import { Solicitacao } from '../../core/models/solicitacao.model';
import { SlaConfig } from '../../core/models/sla.model';
import { COR_PRIORIDADE, FILTRO_SEM_DADOS, PRIORIDADE_LABEL, formatarPrazo } from '../../core/constants/solicitacao.constants';
import { resumirSla, totaisPorPrioridade, totaisPorStatus } from '../../core/utils/dashboard.utils';
import { BottomNavComponent } from '../../shared/components/bottom-nav/bottom-nav';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink, BottomNavComponent],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class DashboardComponent implements OnInit {
  private readonly solicitacaoService = inject(SolicitacaoService);
  private readonly slaService = inject(SlaService);

  readonly prioridadeLabel = PRIORIDADE_LABEL;
  readonly formatarPrazo = formatarPrazo;

  solicitacoes = signal<Solicitacao[]>([]);
  slas = signal<SlaConfig[]>([]);
  loading = signal(true);
  error = signal('');

  total = computed(() => this.solicitacoes().length);
  porStatus = computed(() => totaisPorStatus(this.solicitacoes()));
  porPrioridade = computed(() => totaisPorPrioridade(this.solicitacoes()));
  resumoSla = computed(() => resumirSla(this.solicitacoes(), new Date()));

  corDaPrioridade(valor: string): string {
    return COR_PRIORIDADE[valor] ?? FILTRO_SEM_DADOS.cor;
  }

  ngOnInit(): void {
    forkJoin({
      solicitacoes: this.solicitacaoService.listar(),
      slas: this.slaService.listar()
    }).subscribe({
      next: ({ solicitacoes, slas }) => {
        this.solicitacoes.set(solicitacoes);
        this.slas.set(slas);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Não foi possível carregar o dashboard.');
        this.loading.set(false);
      }
    });
  }
}
