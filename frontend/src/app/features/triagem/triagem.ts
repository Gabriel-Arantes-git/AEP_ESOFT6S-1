import { Component, inject, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { DepartamentoService } from '../../core/services/departamento.service';
import { SolicitacaoService } from '../../core/services/solicitacao.service';
import { DepartamentoDestino } from '../../core/models/departamento.model';
import { Solicitacao } from '../../core/models/solicitacao.model';
import {
  PRIORIDADES,
  PRIORIDADE_LABEL,
  STATUS_OPCOES,
  proximoStatus
} from '../../core/constants/solicitacao.constants';
import { BottomNavComponent } from '../../shared/components/bottom-nav/bottom-nav';

@Component({
  selector: 'app-triagem',
  standalone: true,
  imports: [ReactiveFormsModule, BottomNavComponent],
  templateUrl: './triagem.html',
  styleUrl: './triagem.scss'
})
export class TriagemComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly solicitacaoService = inject(SolicitacaoService);
  private readonly departamentoService = inject(DepartamentoService);

  readonly prioridades = PRIORIDADES;
  readonly prioridadeLabel = PRIORIDADE_LABEL;

  fila = signal<Solicitacao[]>([]);
  departamentos = signal<DepartamentoDestino[]>([]);
  selecionada = signal<Solicitacao | null>(null);
  proximo = signal<string | null>(null);
  loading = signal(false);
  error = signal('');
  sucesso = signal('');

  form = new FormGroup({
    prioridade: new FormControl('', { nonNullable: true }),
    departamentoId: new FormControl('', { nonNullable: true }),
    comentario: new FormControl('', { nonNullable: true, validators: [Validators.required] })
  });

  ngOnInit(): void {
    this.carregarFila();
    this.departamentoService.listarAtivos().subscribe({
      next: departamentos => this.departamentos.set(departamentos),
      error: () => this.error.set('Não foi possível carregar os departamentos.')
    });
  }

  rotuloStatus(status: string): string {
    return STATUS_OPCOES.find(s => s.valor === status)?.label ?? status;
  }

  selecionar(solicitacao: Solicitacao): void {
    const proximo = proximoStatus(solicitacao.status);
    const triagem = proximo === 'TRIAGEM';
    this.selecionada.set(solicitacao);
    this.proximo.set(proximo);
    this.error.set('');
    this.sucesso.set('');
    this.form.reset({
      prioridade: solicitacao.prioridade ?? '',
      departamentoId: solicitacao.departamento?.id ?? '',
      comentario: ''
    });
    this.definirObrigatorio(this.form.controls.prioridade, triagem);
    this.definirObrigatorio(this.form.controls.departamentoId, triagem);
  }

  encerramentoBloqueado(): boolean {
    return this.proximo() === 'ENCERRADO' && !this.authService.isGestor();
  }

  podeMover(): boolean {
    return !!this.proximo() && !this.encerramentoBloqueado() && this.form.valid && !this.loading();
  }

  mover(): void {
    const solicitacao = this.selecionada();
    const novoStatus = this.proximo();
    if (!solicitacao || !novoStatus || !this.podeMover()) return;

    const { prioridade, departamentoId, comentario } = this.form.getRawValue();
    this.loading.set(true);
    this.error.set('');
    this.sucesso.set('');

    this.solicitacaoService.moverStatus(solicitacao.id, {
      novoStatus,
      comentario,
      prioridade: prioridade || null,
      departamentoId: departamentoId || null
    }).subscribe({
      next: () => {
        this.loading.set(false);
        this.selecionada.set(null);
        this.proximo.set(null);
        this.sucesso.set(`Solicitação ${solicitacao.protocolo} movida para ${this.rotuloStatus(novoStatus)}.`);
        this.carregarFila();
      },
      error: () => {
        this.loading.set(false);
        this.error.set('Não foi possível mover a solicitação.');
      }
    });
  }

  private carregarFila(): void {
    this.solicitacaoService.listar().subscribe({
      next: solicitacoes => this.fila.set(solicitacoes.filter(s => s.status !== 'ENCERRADO')),
      error: () => this.error.set('Não foi possível carregar a fila.')
    });
  }

  private definirObrigatorio(controle: FormControl<string>, obrigatorio: boolean): void {
    if (obrigatorio) controle.addValidators(Validators.required);
    else controle.clearValidators();
    controle.updateValueAndValidity();
  }
}
