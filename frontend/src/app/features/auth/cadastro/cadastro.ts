import { Component, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { CadastroRequest } from '../../../core/models/auth.model';
import { cpfValidator, senhaForteValidator, senhasIguaisValidator } from '../../../core/utils/form.validators';

@Component({
  selector: 'app-cadastro',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './cadastro.html',
  styleUrl: './cadastro.scss'
})
export class CadastroComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  loading = signal(false);
  error = signal('');
  sucesso = signal(false);
  submitAttempted = signal(false);

  form = new FormGroup(
    {
      nome: new FormControl('', [Validators.required, Validators.minLength(3)]),
      email: new FormControl('', [Validators.required, Validators.email]),
      cpf: new FormControl('', [Validators.required, cpfValidator]),
      telefone: new FormControl(''),
      senha: new FormControl('', [Validators.required, senhaForteValidator]),
      confirmarSenha: new FormControl('', [Validators.required]),
    },
    { validators: senhasIguaisValidator }
  );

  get senhaAtual(): string {
    return this.form.get('senha')?.value ?? '';
  }

  get senhaRequisitos() {
    const v = this.senhaAtual;
    return {
      tamanho:   v.length >= 8,
      maiuscula: /[A-Z]/.test(v),
      minuscula: /[a-z]/.test(v),
      numero:    /\d/.test(v),
      especial:  /[@$!%*?&._\-#^]/.test(v),
    };
  }

  get senhaErro(): string {
    const erros = this.form.get('senha')?.errors;
    if (!erros || !this.form.get('senha')?.touched) return '';
    if (erros['required']) return 'Senha é obrigatória.';
    if (erros['minLength']) return 'Mínimo 8 caracteres.';
    if (erros['semMaiuscula']) return 'Adicione ao menos uma letra maiúscula.';
    if (erros['semMinuscula']) return 'Adicione ao menos uma letra minúscula.';
    if (erros['semNumero']) return 'Adicione ao menos um número.';
    if (erros['semEspecial']) return 'Adicione ao menos um caractere especial (@$!%*?&._-#^).';
    return '';
  }

  get cpfErro(): boolean {
    const ctrl = this.form.get('cpf');
    return !!(ctrl?.invalid && ctrl.touched);
  }

  get senhasDiferentes(): boolean {
    return !!(this.form.errors?.['senhasDiferentes'] && this.form.get('confirmarSenha')?.touched);
  }

  mascaraCpf(event: Event): void {
    const input = event.target as HTMLInputElement;
    let v = input.value.replace(/\D/g, '').slice(0, 11);
    if (v.length > 9) v = `${v.slice(0, 3)}.${v.slice(3, 6)}.${v.slice(6, 9)}-${v.slice(9)}`;
    else if (v.length > 6) v = `${v.slice(0, 3)}.${v.slice(3, 6)}.${v.slice(6)}`;
    else if (v.length > 3) v = `${v.slice(0, 3)}.${v.slice(3)}`;
    input.value = v;
    this.form.get('cpf')!.setValue(v);
  }

  mascaraTelefone(event: Event): void {
    const input = event.target as HTMLInputElement;
    let v = input.value.replace(/\D/g, '').slice(0, 11);
    if (v.length > 10) v = `(${v.slice(0, 2)}) ${v.slice(2, 7)}-${v.slice(7)}`;
    else if (v.length > 6) v = `(${v.slice(0, 2)}) ${v.slice(2, 6)}-${v.slice(6)}`;
    else if (v.length > 2) v = `(${v.slice(0, 2)}) ${v.slice(2)}`;
    input.value = v;
    this.form.get('telefone')!.setValue(v);
  }

  onSubmit(): void {
    this.submitAttempted.set(true);
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.error.set('');

    const v = this.form.value;
    const request: CadastroRequest = {
      nome:      v.nome!,
      email:     v.email!,
      cpf:       (v.cpf ?? '').replace(/\D/g, ''),
      telefone:  (v.telefone ?? '').replace(/\D/g, ''),
      senha:     v.senha!,
      perfil:    'CIDADAO',
    };

    this.authService.cadastrar(request).subscribe({
      next: () => {
        this.sucesso.set(true);
        setTimeout(() => this.router.navigate(['/login']), 2500);
      },
      error: (err: HttpErrorResponse) => {
        const mensagem = String(err?.error?.mensagem ?? '').toLowerCase();
        if (mensagem.includes('e-mail') || mensagem.includes('email')) {
          this.error.set('Este e-mail já está cadastrado.');
        } else if (err?.status === 409) {
          this.error.set('Este CPF ou e-mail já está cadastrado.');
        } else {
          this.error.set('Erro ao criar conta. Tente novamente.');
        }
        this.loading.set(false);
      }
    });
  }
}
