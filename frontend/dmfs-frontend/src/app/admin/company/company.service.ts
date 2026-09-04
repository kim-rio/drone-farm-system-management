import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Company {
  id: number;
  name: string;
  registrationNumber: string;
  tin: string | null;
  email: string | null;
  phone: string | null;
  country: string;
  region: string;
  city: string;
  physicalAddress: string;
  status: string;
  createdAt: string;
  updatedAt: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class CompanyService {

  private readonly http = inject(HttpClient);

  private readonly apiUrl = '/api/admin/company';

  getCompany(): Observable<Company> {
    return this.http.get<Company>(
      this.apiUrl,
      {
        withCredentials: true
      }
    );
  }
}
