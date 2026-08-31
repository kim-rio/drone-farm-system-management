import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Block {
  id: number;
  name: string;
  description: string | null;
  areaHectares: number | null;
  centerLatitude: number | null;
  centerLongitude: number | null;
  farmId: number | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateBlockRequest {
  name: string;
  description?: string;
  areaHectares?: number;
  centerLatitude?: number;
  centerLongitude?: number;
}

@Injectable({
  providedIn: 'root'
})
export class BlockService {

  private readonly http = inject(HttpClient);

  private readonly apiUrl =
    'http://localhost:8080/api/blocks';

  createBlock(
    farmId: number,
    block: CreateBlockRequest
  ): Observable<Block> {

    return this.http.post<Block>(
      `${this.apiUrl}/farm/${farmId}`,
      block,
      {
        withCredentials: true
      }
    );
  }

  getFarmBlocks(
    farmId: number
  ): Observable<Block[]> {

    return this.http.get<Block[]>(
      `${this.apiUrl}/farm/${farmId}`,
      {
        withCredentials: true
      }
    );
  }

  getBlock(id: number): Observable<Block> {

    return this.http.get<Block>(
      `${this.apiUrl}/${id}`,
      {
        withCredentials: true
      }
    );
  }

  deleteBlock(id: number): Observable<void> {

    return this.http.delete<void>(
      `${this.apiUrl}/${id}`,
      {
        withCredentials: true
      }
    );
  }
}