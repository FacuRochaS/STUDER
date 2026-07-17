import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../../config/api.config';
import { User, UserPublic, UserSearchPageResponse, UserUpdateRequestDTO } from './user.model';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private readonly apiUrl = `${API_CONFIG.baseUrl}${API_CONFIG.users}`;

  constructor(private http: HttpClient) { }

  getMe(): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/me`);
  }

  getById(id: number): Observable<UserPublic> {
    return this.http.get<UserPublic>(`${this.apiUrl}/${id}`);
  }

  getByUsername(username: string): Observable<UserPublic> {
    return this.http.get<UserPublic>(`${this.apiUrl}/username/${encodeURIComponent(username)}`);
  }

  searchUsers(query: string, page = 0, size = 10): Observable<UserSearchPageResponse> {
    const encoded = encodeURIComponent(query.trim());
    return this.http.get<UserSearchPageResponse>(`${this.apiUrl}/search?query=${encoded}&page=${page}&size=${size}`);
  }

  getFollowerCount(userId: number): Observable<{count: number}> {
    return this.http.get<{count: number}>(`${this.apiUrl}/${userId}/followers`);
  }

  updateMe(request?: UserUpdateRequestDTO, file?: File | null): Observable<User> {
    const formData = new FormData();
    if (request) {
      formData.append('request', new Blob([JSON.stringify(request)], { type: 'application/json' }));
    }
    if (file) {
      formData.append('file', file);
    }
    return this.http.put<User>(`${this.apiUrl}`, formData);
  }
}
