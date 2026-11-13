import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LogService } from '../../core/services/log.service';
import { SolicitacaoService } from '../../core/services/solicitacao.service';
import { LogAcao } from '../../core/models/log.model';
import { Solicitacao } from '../../core/models/solicitacao.model';
import { BottomNavComponent } from '../../shared/components/bottom-nav/bottom-nav';

@Component({
  selector: 'app-logs',
  standalone: true,
  imports: [RouterLink, BottomNavComponent],
  templateUrl: './logs.html',
  styleUrl: './logs.scss'
})
export class LogsComponent implements OnInit {
  private readonly logService = inject(LogService);
  private readonly solicitacaoService = inject(SolicitacaoService);

  solicitacoes = signal<Solicitacao[]>([]);
  logs = signal<LogAcao[]>([]);
  solicitacaoId = signal('');
  loading = signal(false);
  error = signal('');

  ngOnInit(): void {
    this.solicitacaoService.listar().subscribe({
      next: solicitacoes => this.solicitacoes.set(solicitacoes),
      error: () => this.error.set('Não foi possível carregar as solicitações.')
    });
    this.carregarLogs();
  }

  filtrarPorSolicitacao(id: string): void {
    this.solicitacaoId.set(id);
    this.carregarLogs();
  }

  private carregarLogs(): void {
    const id = this.solicitacaoId();
    const obs = id ? this.solicitacaoService.logs(id) : this.logService.listar();
    this.loading.set(true);
    this.error.set('');

    obs.subscribe({
      next: logs => {
        this.logs.set(logs);
        this.loading.set(false);
      },
      error: () => {
        this.logs.set([]);
        this.error.set('Não foi possível carregar os logs.');
        this.loading.set(false);
      }
    });
  }
}
