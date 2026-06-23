import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, timer } from 'rxjs';
import { switchMap, filter } from 'rxjs/operators';
import { AuthStateService } from '../auth/auth-state.service';
import { API_CONFIG } from '../../config/api.config';

export interface AppNotification {
  type: 'MESSAGE' | 'DISCUSSION' | 'BLOCK' | 'COURSE' | 'CONTEST';
  payload: any;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private readonly apiUrl = `${API_CONFIG.baseUrl}/notifications`;
  private notificationSubject = new BehaviorSubject<AppNotification[]>([]);
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
      switchMap(() => this.http.get<AppNotification[]>(`${this.apiUrl}/pending`))
    ).subscribe(notifications => {
      if (notifications && notifications.length > 0) {
        this.notificationSubject.next(notifications);
      }
    });
  }

  public getNotifications(): Observable<AppNotification[]> {
    return this.notifications$;
  }
}
