import { NotificationResponseDTO } from '../notification.model';

export interface NotificationStrategy {
  getRoute(notification: NotificationResponseDTO): string;
  getIcon(notification: NotificationResponseDTO): string;
  getQueryParams(notification: NotificationResponseDTO): Record<string, any>;
}
