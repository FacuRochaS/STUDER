import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { API_CONFIG } from '../../config/api.config';
import {
  LinkedType,
  MessageResponseDTO,
  NotificationPageResponseDTO,
  NotificationResponseDTO
} from './notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly unreadCountSubject = new BehaviorSubject<number>(0);
  readonly unreadCount$ = this.unreadCountSubject.asObservable();

  constructor(private readonly http: HttpClient) {}

  /**
   * Obtiene las notificaciones paginadas del usuario autenticado
   */
  getNotifications(
    page = 0,
    type?: LinkedType,
    read?: boolean,
    lastDays?: number
  ): Observable<NotificationPageResponseDTO> {
    let params = new HttpParams().set('page', page.toString());

    if (type) {
      params = params.set('type', type);
    }
    if (read !== undefined) {
      params = params.set('read', read.toString());
    }
    if (lastDays !== undefined) {
      params = params.set('lastDays', lastDays.toString());
    }

    return this.http.get<NotificationPageResponseDTO>(
      `${API_CONFIG.baseUrl}${API_CONFIG.notifications}`,
      { params }
    );
  }

  /**
   * Marca una notificación como leída
   */
  markAsRead(id: number): Observable<MessageResponseDTO> {
    return this.http.patch<MessageResponseDTO>(
      `${API_CONFIG.baseUrl}${API_CONFIG.notifications}/${id}/read`,
      {}
    );
  }

  /**
   * Carga el conteo de notificaciones no leídas
   */
  loadUnreadCount(): void {
    this.getNotifications(0, undefined, false).subscribe({
      next: (page) => this.unreadCountSubject.next(page.totalElements),
      error: () => this.unreadCountSubject.next(0)
    });
  }

  /**
   * Actualiza manualmente el conteo
   */
  decrementUnread(): void {
    const current = this.unreadCountSubject.value;
    if (current > 0) {
      this.unreadCountSubject.next(current - 1);
    }
  }
}

