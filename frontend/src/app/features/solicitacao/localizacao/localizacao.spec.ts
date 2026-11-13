import { TestBed } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { Router, provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { LocalizacaoComponent } from './localizacao';
import { AuthService } from '../../../core/services/auth.service';
import { GeocodingService } from '../../../core/services/geocoding.service';
import { SolicitacaoService } from '../../../core/services/solicitacao.service';
import { SolicitacaoRascunhoService } from '../../../core/services/solicitacao-rascunho.service';

describe('LocalizacaoComponent', () => {
  const reverter = vi.fn();
  const criar = vi.fn();
  const criarAnonima = vi.fn();

  const endereco = { logradouro: 'Rua XV de Novembro', bairro: 'Centro', cep: '87013-230' };

  const montar = async (autenticado: boolean) => {
    TestBed.configureTestingModule({
      imports: [LocalizacaoComponent],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: { isAuthenticated: () => autenticado, isGestor: () => false, isAdmin: () => false, logout: vi.fn() } },
        { provide: GeocodingService, useValue: { reverter } },
        { provide: SolicitacaoService, useValue: { criar, criarAnonima } },
      ],
    });
    TestBed.inject(SolicitacaoRascunhoService).atualizar({
      categoria: 'Lixo Irregular',
      categoriaId: 'cat-1',
      descricao: 'Descarte irregular de lixo na calçada há mais de uma semana sem coleta.',
      pontoReferencia: 'Em frente ao mercado',
    });
    const fixture = TestBed.createComponent(LocalizacaoComponent);
    const navegar = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    const component = fixture.componentInstance;
    fixture.detectChanges();
    await vi.waitFor(() => expect(component['map']).not.toBeNull());
    return { fixture, component, el: fixture.nativeElement as HTMLElement, navegar };
  };

  const clicarNoMapa = async (component: LocalizacaoComponent, lat: number, lng: number) => {
    const L = await import('leaflet');
    component['map']!.fire('click', { latlng: L.latLng(lat, lng) });
  };

  const erroHttp = (mensagem?: string) =>
    throwError(() => new HttpErrorResponse({ status: 400, error: mensagem ? { mensagem } : null }));

  beforeEach(() => {
    localStorage.clear();
    reverter.mockReset();
    criar.mockReset();
    criarAnonima.mockReset();
    reverter.mockReturnValue(of(endereco));
  });

  afterEach(() => vi.useRealTimers());

  it('deve iniciar o mapa Leaflet no container', async () => {
    const { el } = await montar(true);

    expect(el.querySelector('.mapa-container')?.classList).toContain('leaflet-container');
  });

  it('confirmar deve começar desabilitado até marcar um ponto', async () => {
    const { el } = await montar(true);

    expect(el.querySelector<HTMLButtonElement>('.btn-confirmar')?.disabled).toBe(true);
  });

  it('clicar no mapa deve marcar o ponto e preencher o endereço pelo geocoding', async () => {
    const { component, fixture, el } = await montar(true);

    await clicarNoMapa(component, -23.4205, -51.9331);
    fixture.detectChanges();

    expect(reverter).toHaveBeenCalledWith(-23.4205, -51.9331);
    expect(component.pontoSelecionado()).toBe(true);
    expect(component.carregandoEndereco()).toBe(false);
    expect(el.querySelector('.endereco-card')?.textContent).toContain('Rua XV de Novembro');
    expect(el.querySelector('.endereco-card')?.textContent).toContain('87013-230');
    expect(el.querySelectorAll('.leaflet-marker-icon').length).toBe(1);
  });

  it('clicar de novo no mapa deve trocar o marcador', async () => {
    const { component, el } = await montar(true);

    await clicarNoMapa(component, -23.4205, -51.9331);
    await clicarNoMapa(component, -23.43, -51.94);

    expect(el.querySelectorAll('.leaflet-marker-icon').length).toBe(1);
  });

  it('erro no geocoding deve encerrar o carregamento do endereço', async () => {
    reverter.mockReturnValue(throwError(() => new Error('falha')));
    const { component } = await montar(true);

    await clicarNoMapa(component, -23.4205, -51.9331);

    expect(component.carregandoEndereco()).toBe(false);
    expect(component.bairro()).toBe('');
  });

  it('cidadão identificado deve enviar a localização aninhada e receber o protocolo', async () => {
    criar.mockReturnValue(of({ protocolo: 'DEN-2026-00001' }));
    const { component, fixture, el, navegar } = await montar(true);
    await clicarNoMapa(component, -23.4205, -51.9331);
    vi.useFakeTimers();

    component.confirmar();
    fixture.detectChanges();

    expect(criar).toHaveBeenCalledWith({
      categoriaId: 'cat-1',
      descricao: 'Descarte irregular de lixo na calçada há mais de uma semana sem coleta.',
      localizacao: {
        logradouro: 'Rua XV de Novembro',
        bairro: 'Centro',
        cep: '87013-230',
        referencia: 'Em frente ao mercado',
        latitude: -23.4205,
        longitude: -51.9331,
      },
      anonimo: false,
    });
    expect(criarAnonima).not.toHaveBeenCalled();
    expect(el.querySelector('.sucesso-message__protocolo')?.textContent).toBe('DEN-2026-00001');
    expect(TestBed.inject(SolicitacaoRascunhoService).rascunho().categoriaId).toBe('');

    vi.advanceTimersByTime(2000);

    expect(navegar).toHaveBeenCalledWith(['/acompanhamento']);
  });

  it('sem login deve abrir a denúncia anônima e receber o protocolo', async () => {
    criarAnonima.mockReturnValue(of({ protocolo: 'DEN-2026-00002' }));
    const { component } = await montar(false);
    await clicarNoMapa(component, -23.4205, -51.9331);

    component.confirmar();

    expect(criarAnonima).toHaveBeenCalledWith(expect.objectContaining({ anonimo: true }));
    expect(criar).not.toHaveBeenCalled();
    expect(component.protocolo()).toBe('DEN-2026-00002');
    expect(component.sucesso()).toBe(true);
  });

  it('erro no envio deve mostrar a mensagem do backend e liberar novo envio', async () => {
    criar.mockReturnValue(erroHttp('Categoria não encontrada: cat-1'));
    const { component, fixture, el } = await montar(true);
    await clicarNoMapa(component, -23.4205, -51.9331);

    component.confirmar();
    fixture.detectChanges();

    expect(el.querySelector('.error-message')?.textContent).toContain('Categoria não encontrada: cat-1');
    expect(component.enviando()).toBe(false);
    expect(component.sucesso()).toBe(false);
  });

  it('erro no envio sem mensagem deve mostrar o texto padrão', async () => {
    criar.mockReturnValue(erroHttp());
    const { component } = await montar(true);
    await clicarNoMapa(component, -23.4205, -51.9331);

    component.confirmar();

    expect(component.erro()).toBe('Erro ao enviar denúncia. Tente novamente.');
  });

  it('voltar deve ir para a etapa anterior', async () => {
    const { component, navegar } = await montar(true);

    component.voltar();

    expect(navegar).toHaveBeenCalledWith(['/solicitacao/nova']);
  });
});
