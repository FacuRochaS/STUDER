import { NotificationStrategy } from './notification.strategy';
import { NotificationResponseDTO } from '../notification.model';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class MessageNotificationStrategy implements NotificationStrategy {
  getRoute(notification: NotificationResponseDTO): string {
    return notification.linkedId ? `/messages/chats/${notification.linkedId}` : '/messages/chats';
  }

  getIcon(notification: NotificationResponseDTO): string {
    return 'pi pi-envelope';
  }

  getQueryParams(notification: NotificationResponseDTO): Record<string, any> {
    return {};
  }
}
