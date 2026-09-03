import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface SuperAdminDashboardResponse {
  totalCompanies: number;
  activeCompanies: number;
  suspendedCompanies: number;
  expiredCompanies: number;
  totalUsers: number;
  activeUsers: number;
  inactiveUsers: number;
}

export interface InitialAdminRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
}

export interface CompanyResponse {
  id: number;
  name: string;
  registrationNumber: string;
  tin: string | null;
  email: string;
  phone: string;
  country: string;
  region: string;
  city: string;
  physicalAddress: string;
  status: CompanyStatus;
  createdAt: string;
  updatedAt: string;
}

export type CompanyStatus = 'ACTIVE' | 'SUSPENDED' | 'EXPIRED';

export interface CreateCompanyRequest {
  name: string;
  registrationNumber: string;
  tin?: string;
  email: string;
  phone: string;
  country: string;
  region: string;
  city: string;
  physicalAddress: string;
  initialAdmin: InitialAdminRequest;
}

export interface UpdateCompanyRequest {
  name: string;
  registrationNumber: string;
  tin?: string;
  email: string;
  phone: string;
  country: string;
  region: string;
  city: string;
  physicalAddress: string;
}

@Injectable({
  providedIn: 'root'
})
export class SuperAdminService {

  private readonly http = inject(HttpClient);

  private readonly baseUrl = '/api/super-admin';

  getDashboard(): Observable<SuperAdminDashboardResponse> {
    return this.http.get<SuperAdminDashboardResponse>(
      `${this.baseUrl}/dashboard`
    );
  }

  getCompanies(): Observable<CompanyResponse[]> {
    return this.http.get<CompanyResponse[]>(
      `${this.baseUrl}/companies`
    );
  }

  getCompany(id: number): Observable<CompanyResponse> {
    return this.http.get<CompanyResponse>(
      `${this.baseUrl}/companies/${id}`
    );
  }

  createCompany(
    request: CreateCompanyRequest
  ): Observable<CompanyResponse> {
    return this.http.post<CompanyResponse>(
      `${this.baseUrl}/companies`,
      request
    );
  }

  updateCompany(
    id: number,
    request: UpdateCompanyRequest
  ): Observable<CompanyResponse> {
    return this.http.put<CompanyResponse>(
      `${this.baseUrl}/companies/${id}`,
      request
    );
  }

  activateCompany(id: number): Observable<CompanyResponse> {
    return this.http.patch<CompanyResponse>(
      `${this.baseUrl}/companies/${id}/activate`,
      {}
    );
  }

  suspendCompany(id: number): Observable<CompanyResponse> {
    return this.http.patch<CompanyResponse>(
      `${this.baseUrl}/companies/${id}/suspend`,
      {}
    );
  }

  expireCompany(id: number): Observable<CompanyResponse> {
    return this.http.patch<CompanyResponse>(
      `${this.baseUrl}/companies/${id}/expire`,
      {}
    );
  }
}
