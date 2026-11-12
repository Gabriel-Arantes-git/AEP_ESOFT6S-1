import { TestBed } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { Router, provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { CadastroComponent } from './cadastro';
import { AuthService } from '../../../core/services/auth.service';

describe('CadastroComponent', () => {
  const cadastrar = vi.fn();

  const montar = () => {
    TestBed.configureTestingModule({
      imports: [CadastroComponent],
      providers: [provideRouter([]), { provide: AuthService, useValue: { cadastrar } }],
    });
    const fixture = TestBed.createComponent(CadastroComponent);
    fixture.detectChanges();
    const navegar = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    return {
      fixture,
      component: fixture.componentInstance,
      el: fixture.nativeElement as HTMLElement,
      navegar,
    };
  };

  const preencherValido = (component: CadastroComponent) =>
    component.form.setValue({
      nome: 'Maria Silva',
      email: 'maria@gov.br',
      cpf: '529.982.247-25',
      telefone: '(41) 99999-8888',
      senha: 'Abcdef1!',
      confirmarSenha: 'Abcdef1!',
    });

  const eventoDeInput = (valor: string) => {
    const input = document.createElement('input');
    input.value = valor;
    return { evento: { target: input } as unknown as Event, input };
  };

  const erroHttp = (status: number, mensagem?: string) =>
    throwError(() => new HttpErrorResponse({ status, error: mensagem ? { mensagem } : null }));

  beforeEach(() => cadastrar.mockReset());
  afterEach(() => vi.useRealTimers());

  it('formulário inválido não deve chamar a API e deve avisar o usuário', () => {
    const { component, fixture, el } = montar();

    component.onSubmit();
    fixture.detectChanges();

    expect(cadastrar).not.toHaveBeenCalled();
    expect(el.textContent).toContain('Verifique os campos marcados antes de continuar.');
  });

  it('envio válido deve mandar CPF e telefone só com dígitos e perfil CIDADAO', () => {
    vi.useFakeTimers();
    cadastrar.mockReturnValue(of(undefined));
    const { component } = montar();

    preencherValido(component);
    component.onSubmit();

    expect(cadastrar).toHaveBeenCalledWith({
      nome: 'Maria Silva',
      email: 'maria@gov.br',
      cpf: '52998224725',
      telefone: '41999998888',
      senha: 'Abcdef1!',
      perfil: 'CIDADAO',
    });
  });

  it('sucesso deve mostrar a mensagem e navegar para /login após 2,5 segundos', () => {
    vi.useFakeTimers();
    cadastrar.mockReturnValue(of(undefined));
    const { component, fixture, el, navegar } = montar();

    preencherValido(component);
    component.onSubmit();
    fixture.detectChanges();

    expect(el.textContent).toContain('Conta criada com sucesso! Redirecionando para o login...');
    expect(navegar).not.toHaveBeenCalled();
    vi.advanceTimersByTime(2500);
    expect(navegar).toHaveBeenCalledWith(['/login']);
  });

  it('e-mail já cadastrado deve mostrar a mensagem específica e liberar o botão', () => {
    cadastrar.mockReturnValue(erroHttp(400, 'E-mail já cadastrado.'));
    const { component } = montar();

    preencherValido(component);
    component.onSubmit();

    expect(component.error()).toBe('Este e-mail já está cadastrado.');
    expect(component.loading()).toBe(false);
  });

  it('409 deve indicar que CPF ou e-mail já existem', () => {
    cadastrar.mockReturnValue(erroHttp(409, 'Registro já existente: violação de índice único.'));
    const { component } = montar();

    preencherValido(component);
    component.onSubmit();

    expect(component.error()).toBe('Este CPF ou e-mail já está cadastrado.');
  });

  it('erro genérico, inclusive sem corpo, deve mostrar a mensagem padrão', () => {
    cadastrar.mockReturnValue(erroHttp(500));
    const { component } = montar();

    preencherValido(component);
    component.onSubmit();

    expect(component.error()).toBe('Erro ao criar conta. Tente novamente.');
  });

  it.each([
    ['5299', '529.9'],
    ['529982', '529.982'],
    ['5299822', '529.982.2'],
    ['52998224725999', '529.982.247-25'],
  ])('máscara de CPF: %s → %s', (digitado, esperado) => {
    const { component } = montar();
    const { evento, input } = eventoDeInput(digitado);

    component.mascaraCpf(evento);

    expect(input.value).toBe(esperado);
    expect(component.form.get('cpf')?.value).toBe(esperado);
  });

  it.each([
    ['41', '41'],
    ['419', '(41) 9'],
    ['4199999', '(41) 9999-9'],
    ['41999998888', '(41) 99999-8888'],
  ])('máscara de telefone: %s → %s', (digitado, esperado) => {
    const { component } = montar();
    const { evento, input } = eventoDeInput(digitado);

    component.mascaraTelefone(evento);

    expect(input.value).toBe(esperado);
    expect(component.form.get('telefone')?.value).toBe(esperado);
  });

  it('checklist de senha deve refletir os requisitos atendidos', () => {
    const { component } = montar();

    component.form.get('senha')?.setValue('abc');
    expect(component.senhaRequisitos).toEqual({
      tamanho: false, maiuscula: false, minuscula: true, numero: false, especial: false,
    });

    component.form.get('senha')?.setValue('Abcdef1!');
    expect(component.senhaRequisitos).toEqual({
      tamanho: true, maiuscula: true, minuscula: true, numero: true, especial: true,
    });
  });

  it.each([
    ['', 'Senha é obrigatória.'],
    ['Ab1!', 'Mínimo 8 caracteres.'],
    ['abcdef1!', 'Adicione ao menos uma letra maiúscula.'],
    ['ABCDEF1!', 'Adicione ao menos uma letra minúscula.'],
    ['Abcdefg!', 'Adicione ao menos um número.'],
    ['Abcdefg1', 'Adicione ao menos um caractere especial (@$!%*?&._-#^).'],
  ])('erro de senha "%s" → "%s"', (senha, mensagem) => {
    const { component } = montar();
    const campo = component.form.get('senha')!;

    campo.setValue(senha);
    campo.markAsTouched();

    expect(component.senhaErro).toBe(mensagem);
  });

  it('senha válida ou ainda não tocada não deve mostrar erro', () => {
    const { component } = montar();

    expect(component.senhaErro).toBe('');
    component.form.get('senha')?.setValue('Abcdef1!');
    component.form.get('senha')?.markAsTouched();
    expect(component.senhaErro).toBe('');
  });

  it('CPF inválido tocado deve sinalizar erro', () => {
    const { component } = montar();
    const cpf = component.form.get('cpf')!;

    cpf.setValue('111.111.111-11');
    cpf.markAsTouched();

    expect(component.cpfErro).toBe(true);
  });

  it('senhas diferentes devem aparecer como erro depois que a confirmação é tocada', () => {
    const { component, fixture, el } = montar();

    component.form.patchValue({ senha: 'Abcdef1!', confirmarSenha: 'Outra123!' });
    component.form.get('confirmarSenha')?.markAsTouched();
    fixture.detectChanges();

    expect(component.senhasDiferentes).toBe(true);
    expect(el.textContent).toContain('As senhas não coincidem.');
  });

  it('campos obrigatórios tocados devem mostrar suas mensagens', () => {
    const { component, fixture, el } = montar();

    component.form.markAllAsTouched();
    fixture.detectChanges();

    expect(el.textContent).toContain('Nome é obrigatório.');
    expect(el.textContent).toContain('E-mail é obrigatório.');
    expect(el.textContent).toContain('Confirmação é obrigatória.');
  });

  it('nome curto e e-mail malformado devem mostrar mensagens específicas', () => {
    const { component, fixture, el } = montar();

    component.form.patchValue({ nome: 'Al', email: 'invalido' });
    component.form.markAllAsTouched();
    fixture.detectChanges();

    expect(el.textContent).toContain('Nome deve ter ao menos 3 caracteres.');
    expect(el.textContent).toContain('Informe um e-mail válido.');
  });
});
