import { NotificationStrategy } from '../notification.strategy';
import {NotificationResponseDTO} from '../../../models/notification.model';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class SystemNotificationStrategy implements NotificationStrategy {
  getRoute(notification: NotificationResponseDTO): string {
    return '/home';
  }

  getIcon(notification: NotificationResponseDTO): string {
    return 'pi pi-info-circle';
  }

  getQueryParams(notification: NotificationResponseDTO): Record<string, any> {
    return {};
  }
}
