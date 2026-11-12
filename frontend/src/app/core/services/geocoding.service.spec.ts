import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { GeocodingResult, GeocodingService } from './geocoding.service';

describe('GeocodingService', () => {
  let service: GeocodingService;
  let http: HttpTestingController;

  const enderecoCompleto = {
    address: {
      road: 'Rua das Flores',
      suburb: 'Centro',
      city: 'Curitiba',
      postcode: '80000-000',
      'ISO3166-2-lvl4': 'BR-PR',
    },
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(GeocodingService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  const reverter = (): GeocodingResult[] => {
    const resultado: GeocodingResult[] = [];
    service.reverter(-25.43, -49.27).subscribe(r => resultado.push(r));
    return resultado;
  };

  const nominatim = () => http.expectOne(r => r.url.startsWith('https://nominatim.openstreetmap.org/reverse'));
  const viaCep = () => http.expectOne(r => r.url.startsWith('https://viacep.com.br/ws/'));

  it('deve consultar Nominatim com lat/lon e completar o CEP pelo ViaCEP', () => {
    const resultado = reverter();

    const reqNominatim = nominatim();
    expect(reqNominatim.request.url).toContain('lat=-25.43');
    expect(reqNominatim.request.url).toContain('lon=-49.27');
    reqNominatim.flush(enderecoCompleto);

    const reqViaCep = viaCep();
    expect(reqViaCep.request.url).toBe('https://viacep.com.br/ws/PR/Curitiba/Rua%20das%20Flores/json/');
    reqViaCep.flush([{ cep: '80010-010', logradouro: 'Rua das Flores', bairro: 'Centro', localidade: 'Curitiba', uf: 'PR' }]);

    expect(resultado).toEqual([{ logradouro: 'Rua das Flores', bairro: 'Centro', cep: '80010-010' }]);
  });

  it('deve usar o CEP do Nominatim quando o ViaCEP não encontra o logradouro', () => {
    const resultado = reverter();
    nominatim().flush(enderecoCompleto);
    viaCep().flush({ erro: true });

    expect(resultado).toEqual([{ logradouro: 'Rua das Flores', bairro: 'Centro', cep: '80000-000' }]);
  });

  it('deve usar o CEP do Nominatim quando o ViaCEP falha', () => {
    const resultado = reverter();
    nominatim().flush(enderecoCompleto);
    viaCep().error(new ProgressEvent('error'));

    expect(resultado).toEqual([{ logradouro: 'Rua das Flores', bairro: 'Centro', cep: '80000-000' }]);
  });

  it('não deve chamar o ViaCEP quando faltam logradouro, cidade ou UF', () => {
    const resultado = reverter();
    nominatim().flush({ address: { suburb: 'Centro', postcode: '80000-000' } });

    http.expectNone(r => r.url.startsWith('https://viacep.com.br/'));
    expect(resultado).toEqual([{ logradouro: '', bairro: 'Centro', cep: '80000-000' }]);
  });

  it('deve aceitar nomes alternativos de rua, bairro e cidade', () => {
    const resultado = reverter();
    nominatim().flush({
      address: { pedestrian: 'Calçadão', neighbourhood: 'Batel', town: 'Pinhais', 'ISO3166-2-lvl4': 'BR-PR' },
    });
    viaCep().flush([]);

    expect(resultado).toEqual([{ logradouro: 'Calçadão', bairro: 'Batel', cep: '' }]);
  });
});
