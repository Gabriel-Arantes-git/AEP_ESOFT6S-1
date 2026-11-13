import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { NovaSolicitacaoComponent } from './nova-solicitacao';
import { AuthService } from '../../../core/services/auth.service';
import { CategoriaService } from '../../../core/services/categoria.service';
import { SolicitacaoRascunhoService } from '../../../core/services/solicitacao-rascunho.service';
import { Categoria } from '../../../core/models/categoria.model';

describe('NovaSolicitacaoComponent', () => {
  const categorias: Categoria[] = [
    { id: 'cat-1', nome: 'Lixo Irregular' },
    { id: 'cat-2', nome: 'Queimada' },
  ];
  const listarAtivas = vi.fn();

  const montar = (autenticado: boolean) => {
    TestBed.configureTestingModule({
      imports: [NovaSolicitacaoComponent],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: { isAuthenticated: () => autenticado, isGestor: () => false, isAdmin: () => false, logout: vi.fn() } },
        { provide: CategoriaService, useValue: { listarAtivas } },
      ],
    });
    const fixture = TestBed.createComponent(NovaSolicitacaoComponent);
    const navegar = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    fixture.detectChanges();
    return {
      fixture,
      component: fixture.componentInstance,
      el: fixture.nativeElement as HTMLElement,
      navegar,
      rascunho: TestBed.inject(SolicitacaoRascunhoService),
    };
  };

  const descricaoLonga = 'Descarte irregular de lixo na calçada há mais de uma semana sem coleta.';

  beforeEach(() => {
    localStorage.clear();
    listarAtivas.mockReset();
    listarAtivas.mockReturnValue(of(categorias));
  });

  it('deve listar as categorias ativas no select', () => {
    const { el } = montar(true);

    const opcoes = Array.from(el.querySelectorAll('#categoria option')).map(o => o.textContent?.trim());

    expect(listarAtivas).toHaveBeenCalled();
    expect(opcoes).toEqual(['...', 'Lixo Irregular', 'Queimada']);
  });

  it('deve restaurar o rascunho salvo no formulário', () => {
    localStorage.setItem('aep_rascunho', JSON.stringify({
      categoria: 'Queimada',
      categoriaId: 'cat-2',
      descricao: 'Fogo no terreno baldio',
      pontoReferencia: 'Ao lado da escola',
    }));

    const { component } = montar(true);

    expect(component.form.value).toEqual({
      categoria: 'Queimada',
      descricao: 'Fogo no terreno baldio',
      pontoReferencia: 'Ao lado da escola',
    });
  });

  it('cidadão identificado não precisa de descrição mínima', () => {
    const { component } = montar(true);

    component.form.setValue({ categoria: 'Queimada', descricao: 'Fogo', pontoReferencia: 'Praça' });

    expect(component.anonimo).toBe(false);
    expect(component.form.valid).toBe(true);
  });

  it('denúncia anônima deve exigir descrição com no mínimo 50 caracteres', () => {
    const { component, el, fixture } = montar(false);

    component.form.setValue({ categoria: 'Queimada', descricao: 'Fogo', pontoReferencia: 'Praça' });
    fixture.detectChanges();

    expect(component.anonimo).toBe(true);
    expect(component.form.controls.descricao.hasError('minlength')).toBe(true);
    expect(el.querySelector('.form-hint')?.textContent).toContain('4/50');

    component.form.controls.descricao.setValue(descricaoLonga);

    expect(component.form.valid).toBe(true);
  });

  it('próxima com formulário inválido não deve avançar', () => {
    const { component, navegar, rascunho } = montar(true);

    component.proxima();

    expect(navegar).not.toHaveBeenCalled();
    expect(rascunho.rascunho().categoria).toBe('');
  });

  it('próxima deve salvar o rascunho com o id da categoria e ir para a localização', () => {
    const { component, navegar, rascunho } = montar(false);

    component.form.setValue({ categoria: 'Lixo Irregular', descricao: descricaoLonga, pontoReferencia: 'Em frente ao mercado' });
    component.proxima();

    expect(rascunho.rascunho()).toEqual({
      categoria: 'Lixo Irregular',
      categoriaId: 'cat-1',
      descricao: descricaoLonga,
      pontoReferencia: 'Em frente ao mercado',
    });
    expect(navegar).toHaveBeenCalledWith(['/solicitacao/nova/localizacao']);
  });

  it('voltar deve ir para a home', () => {
    const { component, navegar } = montar(true);

    component.voltar();

    expect(navegar).toHaveBeenCalledWith(['/home']);
  });
});
