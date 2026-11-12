import { Component, inject, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { AuthResponse } from '../../core/models/auth.model';
import { SolicitacaoService } from '../../core/services/solicitacao.service';
import { paraSolicitacaoView } from '../../core/models/solicitacao.model';
import { BottomNavComponent } from '../../shared/components/bottom-nav/bottom-nav';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [BottomNavComponent],
  templateUrl: './home.html',
  styleUrl: './home.scss'
})
export class HomeComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly solicitacaoService = inject(SolicitacaoService);

  user = signal<AuthResponse | null>(null);
  tratadasNaSemana = signal<number>(0);
  abertas = signal<number>(0);

  ngOnInit(): void {
    this.user.set(this.authService.getCurrentUser());
    this.carregarEstatisticas();
  }

  private carregarEstatisticas(): void {
    const obs = this.authService.isAdmin()
      ? this.solicitacaoService.listar()
      : this.solicitacaoService.listarPublicas();

    obs.subscribe({
      next: solicitacoes => {
        const lista = solicitacoes.map(paraSolicitacaoView);
        const umaSemanaAtras = Date.now() - 7 * 24 * 60 * 60 * 1000;

        this.abertas.set(
          lista.filter(s => s.status === 'ABERTO').length
        );
        this.tratadasNaSemana.set(
          lista.filter(s =>
            (s.status === 'RESOLVIDO' || s.status === 'ENCERRADO') &&
            s.dataAtualizacao !== null &&
            new Date(s.dataAtualizacao).getTime() >= umaSemanaAtras
          ).length
        );
      },
      // Sem as estatísticas a tela segue utilizável: os contadores ficam em 0.
      error: () => undefined,
    });
  }

  novaSolicitacao(): void {
    this.router.navigate(['/solicitacao/nova']);
  }

  acompanhamento(): void {
    this.router.navigate(['/acompanhamento']);
  }

  mapa(): void {
    this.router.navigate(['/mapa']);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
