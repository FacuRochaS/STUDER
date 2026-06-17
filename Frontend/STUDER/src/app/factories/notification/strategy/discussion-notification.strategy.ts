import { NotificationStrategy } from '../notification.strategy';
import {NotificationResponseDTO} from '../../../models/notification.model';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class DiscussionNotificationStrategy implements NotificationStrategy {
  getRoute(notification: NotificationResponseDTO): string {
    return notification.linkedId ? `/discussions/${notification.linkedId}` : '/discussions';
  }

  getIcon(notification: NotificationResponseDTO): string {
    return 'pi pi-comments';
  }

  getQueryParams(notification: NotificationResponseDTO): Record<string, any> {
    return {};
  }
}
