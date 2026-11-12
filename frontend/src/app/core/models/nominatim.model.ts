export interface NominatimReverseResponse {
  address: {
    road?: string;
    pedestrian?: string;
    suburb?: string;
    neighbourhood?: string;
    city_district?: string;
    quarter?: string;
    postcode?: string;
    city?: string;
    town?: string;
    municipality?: string;
    'ISO3166-2-lvl4'?: string;
  };
}
