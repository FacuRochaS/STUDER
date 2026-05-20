import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../../config/api.config';
import {
  UserResponseDTO,
  LoginRequestDTO,
  LoginResponseDTO,
  RefreshResponseDTO,
  UserCreateRequestDTO
} from '../../features/users/user.model';

@Injectable({ providedIn: 'root' })
export class AuthApiService {
  constructor(private http: HttpClient) {}

  me(): Observable<UserResponseDTO> {
    return this.http.get<UserResponseDTO>(`${API_CONFIG.baseUrl}${API_CONFIG.users}/me`);
  }

  register(data: UserCreateRequestDTO): Observable<UserResponseDTO> {
    return this.http.post<UserResponseDTO>(`${API_CONFIG.baseUrl}${API_CONFIG.users}/register`, data);
  }

  login(data: LoginRequestDTO): Observable<LoginResponseDTO> {
    return this.http.post<LoginResponseDTO>(`${API_CONFIG.baseUrl}${API_CONFIG.auth}/login`, data);
  }

  refresh(): Observable<RefreshResponseDTO> {
    return this.http.post<RefreshResponseDTO>(`${API_CONFIG.baseUrl}${API_CONFIG.auth}/refresh`, {});
  }

  logout(): Observable<void> {
    return this.http.post<void>(`${API_CONFIG.baseUrl}${API_CONFIG.auth}/logout`, {});
  }


}
