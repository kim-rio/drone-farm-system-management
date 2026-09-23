import { Injectable, inject } from '@angular/core';
import {
  HttpClient
} from '@angular/common/http';

import {
  Observable,
  tap
} from 'rxjs';

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

  private readonly http =
    inject(HttpClient);

  private readonly apiUrl =
    '/api/auth';

  private readonly storageKey =
    'dmfs_current_user';

  private currentUser:
    LoginResponse | null =
      this.loadStoredUser();

  login(
    email: string,
    password: string
  ): Observable<LoginResponse> {

    this.clearSession();

    const body = {
      email:
        email
          .trim()
          .toLowerCase(),

      password
    };

    return this.http.post<LoginResponse>(
      `${this.apiUrl}/login`,
      body,
      {
        withCredentials: true
      }
    ).pipe(

      tap(user => {

        this.currentUser = user;

        localStorage.setItem(
          this.storageKey,
          JSON.stringify(user)
        );
      })
    );
  }

  getCurrentUser():
    LoginResponse | null {

    return this.currentUser;
  }

  isLoggedIn(): boolean {
    return this.currentUser !== null;
  }

  hasRole(
    role: string
  ): boolean {

    return this.currentUser?.role === role;
  }

  logout():
    Observable<void> {

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

  clearSession(): void {

    this.currentUser = null;

    localStorage.removeItem(
      this.storageKey
    );
  }

  private loadStoredUser():
    LoginResponse | null {

    try {

      const stored =
        localStorage.getItem(
          this.storageKey
        );

      if (!stored) {
        return null;
      }

      const user =
        JSON.parse(
          stored
        ) as LoginResponse;

      if (
        !user ||
        !user.userId ||
        !user.email ||
        !user.role
      ) {

        localStorage.removeItem(
          this.storageKey
        );

        return null;
      }

      return user;

    } catch {

      localStorage.removeItem(
        this.storageKey
      );

      return null;
    }
  }
}
