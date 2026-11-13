import { Component, OnInit, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { SolicitacaoRascunhoService } from '../../../core/services/solicitacao-rascunho.service';
import { CategoriaService } from '../../../core/services/categoria.service';
import { Categoria } from '../../../core/models/categoria.model';
import { AuthService } from '../../../core/services/auth.service';
import { BottomNavComponent } from '../../../shared/components/bottom-nav/bottom-nav';

const MIN_DESCRICAO_ANONIMA = 50;

@Component({
  selector: 'app-nova-solicitacao',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, BottomNavComponent],
  templateUrl: './nova-solicitacao.html',
  styleUrl: './nova-solicitacao.scss',
})
export class NovaSolicitacaoComponent implements OnInit {
  private readonly rascunho = inject(SolicitacaoRascunhoService);
  private readonly router = inject(Router);
  private readonly categoriaService = inject(CategoriaService);
  private readonly auth = inject(AuthService);

  readonly categorias = signal<Categoria[]>([]);
  readonly anonimo = !this.auth.isAuthenticated();
  readonly minDescricao = this.anonimo ? MIN_DESCRICAO_ANONIMA : 0;

  form = new FormGroup({
    categoria:       new FormControl('', Validators.required),
    descricao:       new FormControl('', this.anonimo
      ? [Validators.required, Validators.minLength(MIN_DESCRICAO_ANONIMA)]
      : [Validators.required]),
    pontoReferencia: new FormControl('', Validators.required),
  });

  ngOnInit(): void {
    const atual = this.rascunho.rascunho();
    if (atual.categoria) this.form.patchValue(atual);

    this.categoriaService.listarAtivas().subscribe({
      next: cats => this.categorias.set(cats),
    });
  }

  voltar(): void {
    this.router.navigate(['/home']);
  }

  proxima(): void {
    if (this.form.invalid) return;
    const categoriaNome = this.form.value.categoria!;
    this.rascunho.atualizar({
      categoria:       categoriaNome,
      categoriaId:     this.categorias().find(c => c.nome === categoriaNome)?.id ?? '',
      descricao:       this.form.value.descricao!,
      pontoReferencia: this.form.value.pontoReferencia!,
    });
    this.router.navigate(['/solicitacao/nova/localizacao']);
  }
}
