import { adicionarTileLayer, marcadorSvg, pinSvg } from './leaflet.utils';

describe('leaflet.utils', () => {
  it('pinSvg deve gerar um SVG com a cor informada', () => {
    const svg = pinSvg('#dc2626');

    expect(svg).toContain('<svg');
    expect(svg).toContain('fill="#dc2626"');
  });

  it('marcadorSvg deve gerar um SVG fixo de marcador', () => {
    const svg = marcadorSvg();

    expect(svg).toContain('<svg');
    expect(svg).toContain('#1a6b3c');
  });

  it('adicionarTileLayer deve criar a camada do OpenStreetMap e adicioná-la ao mapa', () => {
    const addTo = vi.fn();
    const tileLayer = vi.fn(() => ({ addTo }));
    const mapa = {} as never;

    adicionarTileLayer({ tileLayer } as never, mapa);

    expect(tileLayer).toHaveBeenCalledWith(
      'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
      expect.objectContaining({ maxZoom: 19 })
    );
    expect(addTo).toHaveBeenCalledWith(mapa);
  });
});
