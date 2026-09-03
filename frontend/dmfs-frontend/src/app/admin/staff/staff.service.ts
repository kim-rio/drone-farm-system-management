import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export type StaffRole =
  | 'MANAGEMENT'
  | 'GEOLOGIST'
  | 'DRONE_OPERATOR';

export interface StaffMember {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  role: StaffRole;
  active: boolean;
  createdAt: string;
  updatedAt: string | null;
}

export interface CreateStaffRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  role: StaffRole;
}

export interface UpdateStaffRequest {
  firstName: string;
  lastName: string;
  email: string;
  role: StaffRole;
}

@Injectable({
  providedIn: 'root'
})
export class StaffService {

  private readonly http = inject(HttpClient);

  private readonly apiUrl = '/api/admin/staff';

  getStaff(): Observable<StaffMember[]> {
    return this.http.get<StaffMember[]>(
      this.apiUrl,
      {
        withCredentials: true
      }
    );
  }

  createStaff(
    request: CreateStaffRequest
  ): Observable<StaffMember> {
    return this.http.post<StaffMember>(
      this.apiUrl,
      request,
      {
        withCredentials: true
      }
    );
  }

  updateStaff(
    id: number,
    request: UpdateStaffRequest
  ): Observable<StaffMember> {
    return this.http.put<StaffMember>(
      `${this.apiUrl}/${id}`,
      request,
      {
        withCredentials: true
      }
    );
  }

  activateStaff(id: number): Observable<StaffMember> {
    return this.http.patch<StaffMember>(
      `${this.apiUrl}/${id}/activate`,
      {},
      {
        withCredentials: true
      }
    );
  }

  deactivateStaff(id: number): Observable<StaffMember> {
    return this.http.patch<StaffMember>(
      `${this.apiUrl}/${id}/deactivate`,
      {},
      {
        withCredentials: true
      }
    );
  }
}
