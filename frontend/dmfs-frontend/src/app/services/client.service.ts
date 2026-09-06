import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export type ClientType = 'COMPANY' | 'INDIVIDUAL';

export type ClientStatus =
  | 'ACTIVE'
  | 'INACTIVE'
  | 'SUSPENDED';

export interface Client {

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

  status: ClientStatus;

  companyId?: number;

  registeredBy?: number | null;

  createdAt?: string;

  updatedAt?: string | null;
}

export interface CreateClientRequest {

  clientCode: string;

  type: ClientType;

  companyName?: string;
  registrationNumber?: string;
  tin?: string;

  firstName?: string;
  lastName?: string;

  email: string;

  phone: string;

  address?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ClientService {

  private readonly http = inject(HttpClient);

  private readonly apiUrl =
    'http://localhost:8080/api/clients';

  getClients(): Observable<Client[]> {

    return this.http.get<Client[]>(
      this.apiUrl,
      {
        withCredentials: true
      }
    );
  }

  getClient(id: number): Observable<Client> {

    return this.http.get<Client>(
      `${this.apiUrl}/${id}`,
      {
        withCredentials: true
      }
    );
  }

  createClient(
    client: CreateClientRequest
  ): Observable<Client> {

    return this.http.post<Client>(
      this.apiUrl,
      client,
      {
        withCredentials: true
      }
    );
  }

  updateClient(
    id: number,
    client: CreateClientRequest & {
      status: ClientStatus;
    }
  ): Observable<Client> {

    return this.http.put<Client>(
      `${this.apiUrl}/${id}`,
      client,
      {
        withCredentials: true
      }
    );
  }

  deleteClient(id: number): Observable<void> {

    return this.http.delete<void>(
      `${this.apiUrl}/${id}`,
      {
        withCredentials: true
      }
    );
  }
}
