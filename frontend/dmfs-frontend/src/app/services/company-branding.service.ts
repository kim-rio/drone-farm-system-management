import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {
  Observable,
  of,
  shareReplay,
  tap
} from 'rxjs';

export interface CompanyBrandingResponse {
  companyId: number;
  companyName: string;
  logoUrl: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class CompanyBrandingService {

  private readonly http =
    inject(HttpClient);

  private cachedBranding:
    CompanyBrandingResponse | null = null;

  private brandingRequest$:
    Observable<CompanyBrandingResponse> | null = null;

  getBranding():
    Observable<CompanyBrandingResponse> {

    /*
     * Return cached branding immediately.
     *
     * This prevents every dashboard navigation
     * from requesting the company again.
     */
    if (this.cachedBranding) {

      return of(
        this.cachedBranding
      );

    }

    /*
     * If another component is already loading
     * the branding, share that same request.
     */
    if (this.brandingRequest$) {

      return this.brandingRequest$;

    }

    this.brandingRequest$ =
      this.http.get<CompanyBrandingResponse>(
        '/api/company/branding'
      ).pipe(

        tap(branding => {

          this.cachedBranding =
            branding;

        }),

        shareReplay(1)

      );

    return this.brandingRequest$;
  }

  clearCache(): void {

    this.cachedBranding =
      null;

    this.brandingRequest$ =
      null;

  }

}
