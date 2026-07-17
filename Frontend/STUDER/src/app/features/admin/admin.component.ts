import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { Subject, takeUntil } from 'rxjs';
import { AuthStateService } from '../../core/auth/auth-state.service';

@Component({
  selector: 'studer-admin',
  standalone: true,
  imports: [CommonModule, RouterModule, TranslateModule],
  template: `
    <section class="admin">
      <h1>{{ 'admin.title' | translate }}</h1>
      <div class="admin__cards">
        <div class="admin__card">
          <h2>{{ 'admin.contests' | translate }}</h2>
          <p>{{ 'admin.contests_desc' | translate }}</p>
          <a routerLink="/admin/contests/create" class="btn btn--primary">
            {{ 'admin.create_contest' | translate }}
          </a>
        </div>
      </div>
    </section>
  `,
  styles: [`
    .admin { max-width: 800px; margin: 0 auto; padding: 1.5rem; }
    .admin h1 { color: var(--color-text-prim); margin-bottom: 1.5rem; }
    .admin__cards { display: grid; gap: 1rem; }
    .admin__card { background: var(--color-bg); padding: 1.5rem; border-radius: 8px; box-shadow: 0 1px 3px var(--box-shadow-color); }
    .admin__card h2 { color: var(--color-text-prim); margin: 0 0 0.5rem; }
    .admin__card p { color: var(--color-text-secu); margin-bottom: 1rem; }
    .btn { display: inline-block; padding: 0.5rem 1rem; border: none; border-radius: 4px; cursor: pointer; font-size: 0.9rem; text-decoration: none; }
    .btn--primary { background: var(--color-primary); color: var(--color-text-btn); }
  `]
})
export class AdminComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();
  private authState = inject(AuthStateService);
  isAdmin = false;

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
