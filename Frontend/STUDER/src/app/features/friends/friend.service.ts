import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../../config/api.config';
import { FriendResponseDTO, FriendStatusResponseDTO, FriendsListResponseDTO } from './friend.model';

interface MessageResponseDTO {
  success: boolean;
  message: string;
}

@Injectable({ providedIn: 'root' })
export class FriendService {
  private readonly apiUrl = `${API_CONFIG.baseUrl}${API_CONFIG.friends}`;

  constructor(private http: HttpClient) {}

  followUser(userId: number): Observable<FriendResponseDTO> {
    return this.http.post<FriendResponseDTO>(`${this.apiUrl}/follow`, { userId });
  }

  unfollowUser(userId: number): Observable<MessageResponseDTO> {
    return this.http.delete<MessageResponseDTO>(`${this.apiUrl}/follow/${userId}`);
  }

  getFriends(page = 0): Observable<FriendsListResponseDTO> {
    return this.http.get<FriendsListResponseDTO>(`${this.apiUrl}?page=${page}`);
  }

  getFriendStatus(userId: number): Observable<FriendStatusResponseDTO> {
    return this.http.get<FriendStatusResponseDTO>(`${this.apiUrl}/status/${userId}`);
  }
}

