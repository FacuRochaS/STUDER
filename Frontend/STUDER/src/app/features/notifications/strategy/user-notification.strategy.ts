import { NotificationStrategy } from './notification.strategy';
import { NotificationResponseDTO } from '../notification.model';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class UserNotificationStrategy implements NotificationStrategy {
  getRoute(notification: NotificationResponseDTO): string {
    return notification.linkedId ? `/profile/${notification.linkedId}` : '/profile';
  }

  getIcon(notification: NotificationResponseDTO): string {
    return 'pi pi-user';
  }

  getQueryParams(notification: NotificationResponseDTO): Record<string, any> {
    return {};
  }
}
