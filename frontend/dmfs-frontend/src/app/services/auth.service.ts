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

  private readonly storageKey =
    'dmfs_current_user';

  private currentUser: LoginResponse | null =
    this.loadStoredUser();

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

        localStorage.setItem(
          this.storageKey,
          JSON.stringify(user)
        );
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
        this.clearSession();
      })
    );
  }

  isLoggedIn(): boolean {
    return this.currentUser !== null;
  }

  hasRole(role: string): boolean {
    return this.currentUser?.role === role;
  }

  clearSession(): void {

    this.currentUser = null;

    localStorage.removeItem(
      this.storageKey
    );
  }

  private loadStoredUser(): LoginResponse | null {

    try {

      const stored =
        localStorage.getItem(this.storageKey);

      if (!stored) {
        return null;
      }

      return JSON.parse(stored) as LoginResponse;

    } catch (error) {

      console.error(
        'Failed to restore stored user session:',
        error
      );

      localStorage.removeItem(
        this.storageKey
      );

      return null;
    }
  }
}
