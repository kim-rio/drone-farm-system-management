import { Injectable, inject } from '@angular/core';
import {
  HttpClient
} from '@angular/common/http';

import {
  Observable
} from 'rxjs';

export interface PublicCompanyBranding {
  companyId: number;
  companyName: string;
  workspaceSlug: string;
  logoUrl: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class PublicCompanyBrandingService {

  private readonly http =
    inject(HttpClient);

  getBranding():
    Observable<PublicCompanyBranding> {

    return this.http.get<PublicCompanyBranding>(
      '/api/public/company'
    );
  }
}
