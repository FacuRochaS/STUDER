import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../../config/api.config';
import {
  ChatListPageResponseDTO,
  DirectMessagePageResponseDTO,
  DirectMessageRequestDTO,
  DirectMessageResponseDTO
} from './messages.model';

@Injectable({ providedIn: 'root' })
export class MessagesService {
  private readonly apiUrl = `${API_CONFIG.baseUrl}${API_CONFIG.messages}`;

  constructor(private http: HttpClient) {}

  getChats(page = 0): Observable<ChatListPageResponseDTO> {
    return this.http.get<ChatListPageResponseDTO>(`${this.apiUrl}/chats?page=${page}`);
  }

  getChatByUserId(userId: number, page = 0): Observable<DirectMessagePageResponseDTO> {
    return this.http.get<DirectMessagePageResponseDTO>(`${this.apiUrl}/chats/${userId}?page=${page}`);
  }

  getRequests(page = 0): Observable<DirectMessagePageResponseDTO> {
    return this.http.get<DirectMessagePageResponseDTO>(`${this.apiUrl}/requests?page=${page}`);
  }

  sendMessage(request: DirectMessageRequestDTO): Observable<DirectMessageResponseDTO> {
    return this.http.post<DirectMessageResponseDTO>(`${this.apiUrl}`, request);
  }

  markAsRead(senderId: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/mark-as-read/${senderId}`, {});
  }
}

