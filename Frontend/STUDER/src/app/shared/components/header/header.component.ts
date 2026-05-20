import { Component, OnInit, OnDestroy, TemplateRef, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, takeUntil } from 'rxjs';
import { TranslatePipe } from '@ngx-translate/core';
import { LogoComponent } from '../logo/logo.component';
import { OverlayComponent } from '../overlay/overlay.component';
import { NotificationPanelComponent } from '../notification-panel/notification-panel.component';
import { ThemeService, Theme } from '../../../core/theme/theme.service';
import { LanguageService, Language } from '../../../core/i18n/language.service';
import { NotificationService } from '../../../features/notifications/notification.service';
import { AuthStateService } from '../../../core/auth/auth-state.service';

@Component({
  selector: 'studer-header',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TranslatePipe,
    LogoComponent,
    OverlayComponent,
    NotificationPanelComponent
  ],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();

  searchQuery = '';
  unreadCount = 0;

  @ViewChild('notificationContent', { static: true }) notificationContent!: TemplateRef<unknown>;

  constructor(
    private readonly router: Router,
    private readonly themeService: ThemeService,
    private readonly languageService: LanguageService,
    private readonly notificationService: NotificationService,
    private readonly authState: AuthStateService
  ) {}

  ngOnInit(): void {
    this.notificationService.unreadCount$
      .pipe(takeUntil(this.destroy$))
      .subscribe(count => this.unreadCount = count);

    this.notificationService.loadUnreadCount();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  goHome(): void {
    this.router.navigate(['/home']);
  }

  toggleTheme(): void {
    const next: Theme = this.themeService.current === 'light' ? 'dark' : 'light';
    this.themeService.setTheme(next);
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
    // Search logic to be implemented
  }
}

