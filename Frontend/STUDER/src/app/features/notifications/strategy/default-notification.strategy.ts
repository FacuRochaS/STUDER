import { NotificationStrategy } from './notification.strategy';
import { NotificationResponseDTO } from '../notification.model';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class DefaultNotificationStrategy implements NotificationStrategy {
  getRoute(notification: NotificationResponseDTO): string {
    return '/home';
  }

  getIcon(notification: NotificationResponseDTO): string {
    return 'pi pi-bell';
  }

  getQueryParams(notification: NotificationResponseDTO): Record<string, any> {
    return {};
  }
}
