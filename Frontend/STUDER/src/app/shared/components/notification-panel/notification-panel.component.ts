import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { TranslatePipe } from '@ngx-translate/core';
import { NotificationService } from '../../../features/notifications/notification.service';
import {
  LinkedType,
  NotificationResponseDTO,
  NOTIFICATION_ROUTE_MAP
} from '../../../features/notifications/notification.model';
import { RelativeTimePipe } from '../../pipes/relative-time.pipe';

type NotificationFilter = 'ALL' | 'UNREAD';

@Component({
  selector: 'studer-notification-panel',
  standalone: true,
  imports: [CommonModule, TranslatePipe, RelativeTimePipe],
  templateUrl: './notification-panel.component.html',
  styleUrls: ['./notification-panel.component.css']
})
export class NotificationPanelComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();

  notifications: NotificationResponseDTO[] = [];
  loading = false;
  currentPage = 0;
  hasMore = false;
  activeFilter: NotificationFilter = 'ALL';

  constructor(
    private readonly notificationService: NotificationService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.loadNotifications();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadNotifications(append = false): void {
    this.loading = true;
    const readFilter = this.activeFilter === 'UNREAD' ? false : undefined;

    this.notificationService
      .getNotifications(this.currentPage, undefined, readFilter)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (page) => {
          this.notifications = append
            ? [...this.notifications, ...page.notifications]
            : page.notifications;
          this.hasMore = page.hasMore;
          this.loading = false;
        },
        error: () => {
          this.loading = false;
        }
      });
  }

  loadMore(): void {
    this.currentPage++;
    this.loadNotifications(true);
  }

  setFilter(filter: NotificationFilter): void {
    this.activeFilter = filter;
    this.currentPage = 0;
    this.loadNotifications();
  }

  markAsRead(notification: NotificationResponseDTO, event: MouseEvent): void {
    event.stopPropagation();
    if (notification.read) return;

    this.notificationService
      .markAsRead(notification.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          notification.read = true;
          this.notificationService.decrementUnread();
        }
      });
  }

  navigateTo(notification: NotificationResponseDTO): void {
    if (!notification.read) {
      this.notificationService
        .markAsRead(notification.id)
        .pipe(takeUntil(this.destroy$))
        .subscribe(() => {
          this.notificationService.decrementUnread();
        });
    }
    const routePrefix = NOTIFICATION_ROUTE_MAP[notification.type] || '/home';
    this.router.navigate([routePrefix, notification.id]);
  }

  getIconForType(type: LinkedType): string {
    const icons: Record<LinkedType, string> = {
      COURSE: 'pi pi-book',
      DISCUSSION: 'pi pi-megaphone',
      ACTIVITY: 'pi pi-check-square',
      MESSAGE: 'pi pi-envelope',
      USER: 'pi pi-user',
      SYSTEM: 'pi pi-cog'
    };
    return icons[type] || 'pi pi-bell';
  }
}

