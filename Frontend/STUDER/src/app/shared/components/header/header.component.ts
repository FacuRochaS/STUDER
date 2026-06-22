import { Component, OnInit, OnDestroy, TemplateRef, ViewChild, ElementRef, HostListener } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, takeUntil, debounceTime, distinctUntilChanged, switchMap, of, catchError } from 'rxjs';
import { TranslatePipe } from '@ngx-translate/core';
import { LogoComponent } from '../logo/logo.component';
import { OverlayComponent } from '../overlay/overlay.component';
import { NotificationPanelComponent } from '../notification-panel/notification-panel.component';
import { ThemeService, Theme } from '../../../core/theme/theme.service';
import { LanguageService, Language } from '../../../core/i18n/language.service';
import { NotificationService } from '../../../features/notifications/notification.service';
import { AuthStateService } from '../../../core/auth/auth-state.service';
import { UserPublic } from '../../../features/users/user.model';
import { UserService } from '../../../features/users/user.service';
import { RichTextComponent } from '../rich-text/rich-text.component';

@Component({
  selector: 'studer-header',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    TranslatePipe,
    LogoComponent,
    OverlayComponent,
    NotificationPanelComponent,
    RichTextComponent
  ],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();
  private readonly searchInput$ = new Subject<string>();

  searchQuery = '';
  unreadCount = 0;
  searchResults: UserPublic[] = [];
  searchLoading = false;
  isSearchOpen = false;

  @ViewChild('notificationContent', { static: true }) notificationContent!: TemplateRef<unknown>;
  @ViewChild('searchContainer', { static: true }) searchContainer!: ElementRef<HTMLElement>;

  constructor(
    private readonly router: Router,
    private readonly themeService: ThemeService,
    private readonly languageService: LanguageService,
    private readonly notificationService: NotificationService,
    private readonly authState: AuthStateService,
    private readonly userService: UserService
  ) {}

  ngOnInit(): void {
    this.notificationService.unreadCount$
      .pipe(takeUntil(this.destroy$))
      .subscribe(count => this.unreadCount = count);

    this.notificationService.loadUnreadCount();

    this.searchInput$
      .pipe(
        debounceTime(250),
        distinctUntilChanged(),
        switchMap(query => {
          const trimmed = query.trim();
          if (!trimmed) {
            this.searchResults = [];
            this.isSearchOpen = false;
            this.searchLoading = false;
            return of(null);
          }
          this.searchLoading = true;
          this.isSearchOpen = true;
          return this.userService.searchUsers(trimmed, 0, 5).pipe(
            catchError(() => of(null))
          );
        })
      )
      .pipe(takeUntil(this.destroy$))
      .subscribe(result => {
        this.searchResults = result?.users ?? [];
        this.searchLoading = false;
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  goHome(): void {
    this.router.navigate(['/home']);
  }

  toggleTheme(event: MouseEvent) {
    const nextTheme = this.themeService.current === 'light' ? 'dark' : 'light';
    this.themeService.setTheme(nextTheme, { animate: true, event: event });
  }

  get currentThemeIcon(): string {
    return this.themeService.current === 'light' ? 'pi pi-moon' : 'pi pi-sun';
  }

  toggleLanguage(): void {
    const next: Language = this.languageService.current === 'es' ? 'en' : 'es';
    this.languageService.setLanguage(next);
  }

  get currentLanguageLabel(): string {
    return this.languageService.current.toUpperCase();
  }

  logout(): void {
    this.authState.logout()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => this.router.navigate(['/login']),
        error: () => this.router.navigate(['/login'])
      });
  }

  onSearch(): void {
    const trimmed = this.searchQuery.trim();
    if (!trimmed) return;
    this.isSearchOpen = false;
    this.router.navigate(['/search'], { queryParams: { q: trimmed } });
  }

  onSearchInput(): void {
    this.searchInput$.next(this.searchQuery);
  }

  openSearchPanel(): void {
    if (this.searchQuery.trim()) {
      this.isSearchOpen = true;
    }
  }

  closeSearchPanel(): void {
    this.isSearchOpen = false;
  }

  trackByUserId(_: number, user: UserPublic): number {
    return user.id;
  }

  getUserInitials(user: UserPublic): string {
    const first = user.firstName?.[0] ?? '';
    const last = user.lastName?.[0] ?? '';
    const fallback = user.username?.[0] ?? '';
    const initials = `${first}${last}`.trim();
    return (initials || fallback).toUpperCase();
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.searchContainer?.nativeElement.contains(event.target as Node)) {
      this.closeSearchPanel();
    }
  }
}

