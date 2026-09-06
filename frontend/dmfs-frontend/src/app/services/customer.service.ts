import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export type ClientType =
  | 'INDIVIDUAL'
  | 'COMPANY';

export type ClientStatus =
  | 'ACTIVE'
  | 'INACTIVE'
  | 'SUSPENDED';

export interface Customer {
  id: number;
  clientCode: string;
  type: ClientType;

  companyName?: string;
  registrationNumber?: string;
  tin?: string;

  firstName?: string;
  lastName?: string;

  email: string;
  phone?: string;
  address?: string;

  status?: ClientStatus;
  companyId?: number;
  registeredBy?: number | null;

  createdAt?: string;
  updatedAt?: string | null;
}

export interface CreateCustomerRequest {
  clientCode: string;
  type: ClientType;

  companyName?: string;
  registrationNumber?: string;
  tin?: string;

  firstName?: string;
  lastName?: string;

  email: string;
  phone?: string;
  address?: string;
}

@Injectable({
  providedIn: 'root'
})
export class CustomerService {

  private readonly http = inject(HttpClient);

  private readonly apiUrl =
    'http://localhost:8080/api/clients';

  getCustomers(): Observable<Customer[]> {
    return this.http.get<Customer[]>(
      this.apiUrl,
      {
        withCredentials: true
      }
    );
  }

  getCustomer(id: number): Observable<Customer> {
    return this.http.get<Customer>(
      `${this.apiUrl}/${id}`,
      {
        withCredentials: true
      }
    );
  }

  createCustomer(
    customer: CreateCustomerRequest
  ): Observable<Customer> {
    return this.http.post<Customer>(
      this.apiUrl,
      customer,
      {
        withCredentials: true
      }
    );
  }
}
