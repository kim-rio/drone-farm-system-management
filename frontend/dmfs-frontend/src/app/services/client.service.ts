import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export type ClientType =
  | 'INDIVIDUAL'
  | 'EXPLORATION_COMPANY';

export type ClientStatus =
  | 'ACTIVE'
  | 'INACTIVE'
  | 'SUSPENDED';

export interface Client {
  id: number;
  clientCode: string;
  type: ClientType;
  companyName?: string;
  firstName?: string;
  lastName?: string;
  email?: string;
  phone?: string;
  address?: string;
  identificationNumber?: string;
  tin?: string;
  status: ClientStatus;
  companyId: number;
  registeredBy: number | null;
  createdAt: string;
  updatedAt: string | null;
}

export interface CreateClientRequest {
  clientCode: string;
  type: ClientType;
  companyName?: string;
  firstName?: string;
  lastName?: string;
  email?: string;
  phone?: string;
  address?: string;
  identificationNumber?: string;
  tin?: string;
}

export interface UpdateClientRequest {
  type: ClientType;
  companyName?: string;
  firstName?: string;
  lastName?: string;
  email?: string;
  phone?: string;
  address?: string;
  identificationNumber?: string;
  tin?: string;
  status: ClientStatus;
}

@Injectable({
  providedIn: 'root'
})
export class ClientService {

  private readonly http = inject(HttpClient);

  private readonly apiUrl = '/api/clients';

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
    client: UpdateClientRequest
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
