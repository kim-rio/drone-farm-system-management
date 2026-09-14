import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Company {
  id: number;
  name: string;
  registrationNumber: string;
  tin?: string;
  email?: string;
  phone?: string;
  country?: string;
  region?: string;
  city?: string;
  physicalAddress?: string;
  status: string;
  createdAt?: string;
  updatedAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class CompanyService {

  private readonly http = inject(HttpClient);

  getCompany(): Observable<Company> {
    return this.http.get<Company>('/api/admin/company');
  }
}
