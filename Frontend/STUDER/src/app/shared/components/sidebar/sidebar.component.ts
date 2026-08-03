import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { Subject, takeUntil } from 'rxjs';
import { AuthStateService } from '../../../core/auth/auth-state.service';

interface NavItem {
  label: string;
  icon: string;
  route: string;
  colorClass: string;
}

@Component({
  selector: 'studer-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, TranslatePipe],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();
  private readonly authState = inject(AuthStateService);

  isAdmin = false;

  readonly navItems: NavItem[] = [
    { label: 'sidebar.home', icon: 'fa-solid fa-house', route: '/home', colorClass: 'nav--home' },
    { label: 'sidebar.contests', icon: 'fa-solid fa-list-check', route: '/contests', colorClass: 'nav--contest' },
    { label: 'sidebar.courses', icon: 'fa-solid fa-book', route: '/courses', colorClass: 'nav--courses' },
    { label: 'sidebar.discussions', icon: 'fa-solid fa-comments', route: '/discussions', colorClass: 'nav--discussions' },
    { label: 'sidebar.messages', icon: 'fa-solid fa-envelope', route: '/messages', colorClass: 'nav--messages' },
    { label: 'sidebar.account', icon: 'fa-solid fa-user', route: '/user/me', colorClass: 'nav--account' },
  ];

  ngOnInit(): void {
    this.authState.user$.pipe(takeUntil(this.destroy$)).subscribe(u => {
      this.isAdmin = u?.role === 'ADMIN';
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
