import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Customer {
  id: number;
  customerCode: string;
  type: string;
  companyName?: string;
  firstName?: string;
  lastName?: string;
  email: string;
  phone?: string;
  address?: string;
  identificationNumber?: string;
  tin?: string;
  status?: string;
}

export interface CreateCustomerRequest {
  customerCode: string;
  type: string;
  companyName?: string;
  firstName?: string;
  lastName?: string;
  email: string;
  phone?: string;
  address?: string;
  identificationNumber?: string;
  tin?: string;
  password: string;
}

@Injectable({
  providedIn: 'root'
})
export class CustomerService {

  private readonly http = inject(HttpClient);

  private readonly apiUrl =
    'http://localhost:8080/api/customers';

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
