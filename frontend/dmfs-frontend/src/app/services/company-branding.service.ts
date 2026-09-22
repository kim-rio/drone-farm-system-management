import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CompanyBrandingResponse {
  companyId: number;
  companyName: string;
  logoUrl: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class CompanyBrandingService {

  private readonly http = inject(HttpClient);

  getBranding(): Observable<CompanyBrandingResponse> {
    return this.http.get<CompanyBrandingResponse>(
      '/api/company/branding'
    );
  }
}