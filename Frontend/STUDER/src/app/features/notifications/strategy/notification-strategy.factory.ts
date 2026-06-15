import { Injectable, Injector } from '@angular/core';
import { LinkedType } from '../notification.model';
import { NotificationStrategy } from './notification.strategy';
import { DiscussionNotificationStrategy } from './discussion-notification.strategy';
import { MessageNotificationStrategy } from './message-notification.strategy';
import { UserNotificationStrategy } from './user-notification.strategy';
import { SystemNotificationStrategy } from './system-notification.strategy';
import { DefaultNotificationStrategy } from './default-notification.strategy';

@Injectable({
  providedIn: 'root'
})
export class NotificationStrategyFactory {
  private strategies: Map<LinkedType, NotificationStrategy> = new Map();

  constructor(private injector: Injector) {
    this.strategies.set('DISCUSSION', this.injector.get(DiscussionNotificationStrategy));
    this.strategies.set('MESSAGE', this.injector.get(MessageNotificationStrategy));
    this.strategies.set('USER', this.injector.get(UserNotificationStrategy));
    this.strategies.set('SYSTEM', this.injector.get(SystemNotificationStrategy));
    // Add other strategies for COURSE, ACTIVITY, EVENT
  }

  getStrategy(type: LinkedType): NotificationStrategy {
    return this.strategies.get(type) || this.injector.get(DefaultNotificationStrategy);
  }
}
