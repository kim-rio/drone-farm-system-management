import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

export interface LoginResponse {
  token: null;
  userId: number;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly http = inject(HttpClient);

  private readonly apiUrl =
    'http://localhost:8080/api/auth';

  private currentUser: LoginResponse | null = null;

  login(
    email: string,
    password: string
  ): Observable<LoginResponse> {

    return this.http.post<LoginResponse>(
      `${this.apiUrl}/login`,
      {
        email: email,
        password: password
      },
      {
        withCredentials: true
      }
    ).pipe(
      tap((user) => {
        this.currentUser = user;
      })
    );
  }

  getCurrentUser(): LoginResponse | null {
    return this.currentUser;
  }

  logout(): Observable<void> {

    return this.http.post<void>(
      `${this.apiUrl}/logout`,
      {},
      {
        withCredentials: true
      }
    ).pipe(
      tap(() => {
        this.currentUser = null;
      })
    );
  }

  isLoggedIn(): boolean {
    return this.currentUser !== null;
  }
}