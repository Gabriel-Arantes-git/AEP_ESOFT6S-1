export function pinSvg(cor: string): string {
  return `<svg xmlns="http://www.w3.org/2000/svg" width="28" height="36" viewBox="0 0 28 36">
    <path d="M14 0C8.477 0 4 4.477 4 10c0 7.875 10 26 10 26S24 17.875 24 10C24 4.477 19.523 0 14 0z"
      fill="${cor}" stroke="rgba(255,255,255,0.8)" stroke-width="1.5"/>
    <circle cx="14" cy="10" r="4" fill="rgba(255,255,255,0.9)"/>
  </svg>`;
}

export function marcadorSvg(): string {
  return `<svg xmlns="http://www.w3.org/2000/svg" width="32" height="42" viewBox="0 0 32 42">
    <path d="M16 0C9.373 0 4 5.373 4 12c0 9 12 30 12 30S28 21 28 12C28 5.373 22.627 0 16 0z"
      fill="#1a6b3c" stroke="#fff" stroke-width="1.5"/>
    <circle cx="16" cy="12" r="5" fill="#fff"/>
  </svg>`;
}

export function adicionarTileLayer(
  L: typeof import('leaflet'),
  map: import('leaflet').Map,
): void {
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
    maxZoom: 19,
  }).addTo(map);
}
