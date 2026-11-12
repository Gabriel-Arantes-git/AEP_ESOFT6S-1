import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import { NominatimReverseResponse } from '../models/nominatim.model';
import { ViaCepResponse } from '../models/viacep.model';

export interface GeocodingResult {
  logradouro: string;
  bairro: string;
  cep: string;
}

@Injectable({ providedIn: 'root' })
export class GeocodingService {
  private readonly http = inject(HttpClient);

  reverter(lat: number, lng: number): Observable<GeocodingResult> {
    return this.http
      .get<NominatimReverseResponse>(
        `https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${lat}&lon=${lng}&addressdetails=1&accept-language=pt-BR`
      )
      .pipe(
        switchMap(res => {
          const addr = res.address;
          const logradouro = addr.road ?? addr.pedestrian ?? '';
          const bairro = addr.suburb ?? addr.neighbourhood ?? addr.city_district ?? addr.quarter ?? '';
          const cidade = addr.city ?? addr.town ?? addr.municipality ?? '';
          const uf = (addr['ISO3166-2-lvl4'] ?? '').split('-')[1] ?? '';
          const cepFallback = addr.postcode ?? '';

          if (!logradouro || !cidade || !uf) {
            return of({ logradouro, bairro, cep: cepFallback });
          }

          return this.http
            .get<ViaCepResponse[] | { erro: boolean }>(
              `https://viacep.com.br/ws/${uf}/${encodeURIComponent(cidade)}/${encodeURIComponent(logradouro)}/json/`
            )
            .pipe(
              map(result => {
                const cep = Array.isArray(result) && result.length > 0 ? result[0].cep : cepFallback;
                return { logradouro, bairro, cep };
              }),
              catchError(() => of({ logradouro, bairro, cep: cepFallback }))
            );
        })
      );
  }
}
