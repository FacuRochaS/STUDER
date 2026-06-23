import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, timer } from 'rxjs';
import { switchMap, filter, map } from 'rxjs/operators';
import { AuthStateService } from '../auth/auth-state.service';
import { API_CONFIG } from '../../config/api.config';
import { NotificationPageResponseDTO, NotificationResponseDTO } from '../../features/notifications/notification.model';

@Injectable({
  providedIn: 'root'
})
export class NewNotificationService {
  private readonly apiUrl = `${API_CONFIG.baseUrl}${API_CONFIG.notifications}`;
  private notificationSubject = new BehaviorSubject<NotificationResponseDTO[]>([]);
  public notifications$ = this.notificationSubject.asObservable();

  constructor(
    private http: HttpClient,
    private authState: AuthStateService
  ) {
    this.startPolling();
  }

  private startPolling(): void {
    this.authState.user$.pipe(
      filter(user => !!user),
      switchMap(() => timer(0, 10000)), // Poll every 10 seconds
      switchMap(() => this.fetchPendingNotifications())
    ).subscribe(notifications => {
      this.notificationSubject.next(notifications || []);
    });
  }

  private fetchPendingNotifications(): Observable<NotificationResponseDTO[]> {
    return this.http.get<NotificationPageResponseDTO>(`${this.apiUrl}/pending`).pipe(
      map(response => response.notifications)
    );
  }

  public getNotifications(): Observable<NotificationResponseDTO[]> {
    return this.notifications$;
  }
}
