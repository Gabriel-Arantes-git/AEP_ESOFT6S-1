import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { Subject, of, throwError } from 'rxjs';
import { LoginComponent } from './login';
import { AuthService } from '../../../core/services/auth.service';

describe('LoginComponent', () => {
  const login = vi.fn();

  const montar = () => {
    TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [provideRouter([]), { provide: AuthService, useValue: { login } }],
    });
    const fixture = TestBed.createComponent(LoginComponent);
    fixture.detectChanges();
    const navegar = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    return {
      fixture,
      component: fixture.componentInstance,
      el: fixture.nativeElement as HTMLElement,
      navegar,
      botao: () => fixture.nativeElement.querySelector('button[type="submit"]') as HTMLButtonElement,
    };
  };

  beforeEach(() => login.mockReset());

  it('com o formulário vazio deve manter o botão desabilitado e não chamar a API', () => {
    const { component, botao } = montar();

    component.onSubmit();

    expect(botao().disabled).toBe(true);
    expect(login).not.toHaveBeenCalled();
  });

  it('e-mail malformado deve manter o formulário inválido', () => {
    const { component, fixture, botao } = montar();

    component.loginForm.setValue({ email: 'nao-e-email', senha: '123' });
    fixture.detectChanges();

    expect(botao().disabled).toBe(true);
  });

  it('login válido deve chamar o AuthService e navegar para /home', () => {
    login.mockReturnValue(of({ token: 't', email: 'a@gov', nome: 'A', perfil: 'CIDADAO' }));
    const { component, fixture, botao, navegar } = montar();

    component.loginForm.setValue({ email: 'a@gov.br', senha: '123' });
    fixture.detectChanges();
    expect(botao().disabled).toBe(false);
    component.onSubmit();

    expect(login).toHaveBeenCalledWith({ email: 'a@gov.br', senha: '123' });
    expect(navegar).toHaveBeenCalledWith(['/home']);
  });

  it('credenciais inválidas devem mostrar o erro e liberar o botão', () => {
    login.mockReturnValue(throwError(() => new Error('401')));
    const { component, fixture, el, botao, navegar } = montar();

    component.loginForm.setValue({ email: 'a@gov.br', senha: 'errada' });
    component.onSubmit();
    fixture.detectChanges();

    expect(el.querySelector('.error-message')?.textContent).toContain('E-mail ou senha inválidos.');
    expect(component.loading()).toBe(false);
    expect(botao().disabled).toBe(false);
    expect(navegar).not.toHaveBeenCalled();
  });

  it('durante a requisição deve desabilitar o botão e mostrar "Entrando..."', () => {
    login.mockReturnValue(new Subject());
    const { component, fixture, botao } = montar();

    component.loginForm.setValue({ email: 'a@gov.br', senha: '123' });
    component.onSubmit();
    fixture.detectChanges();

    expect(botao().disabled).toBe(true);
    expect(botao().textContent).toContain('Entrando...');
  });

  it('deve oferecer links para cadastro e para continuar sem conta', () => {
    const { el } = montar();

    expect(el.querySelector('a[href="/cadastro"]')?.textContent).toContain('Criar conta');
    expect(el.querySelector('a[href="/home"]')?.textContent).toContain('Continuar sem conta');
  });
});
