import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { Subject, takeUntil } from 'rxjs';
import { AuthStateService } from '../../core/auth/auth-state.service';
import { ContestService } from '../contest/contest.service';
import { DashboardMetricsDTO, ContestResponseDTO } from '../contest/contest.model';
import { LoaderComponent } from '../../shared/components/loader/loader.component';

@Component({
  selector: 'studer-admin',
  standalone: true,
  imports: [CommonModule, RouterModule, TranslateModule, LoaderComponent],
  template: `
    <section class="admin-page">
      <header class="admin-page-header">
        <h1>{{ 'admin.title' | translate }}</h1>
        @if (isAdmin) {
          <div class="admin-header-actions">
            <a routerLink="/admin/contests/create" class="btn btn--primary">{{ 'admin.create_contest' | translate }}</a>
          </div>
        }
      </header>

      @if (!isAdmin) {
        <div class="admin-access-denied">
          <i class="fa-solid fa-lock"></i>
          <p>{{ 'admin.access_denied' | translate }}</p>
        </div>
      } @else if (loading) {
        <studer-loader></studer-loader>
      } @else {
        <!-- Dashboard metrics -->
        <section class="admin-section">
          <h2>{{ 'admin.dashboard' | translate }}</h2>
          @if (metrics) {
            <div class="metrics-grid">
              <div class="metric-card">
                <span class="metric-value">{{ metrics.totalUsers }}</span>
                <span class="metric-label">{{ 'admin.metrics.total_users' | translate }}</span>
              </div>
              <div class="metric-card">
                <span class="metric-value">{{ metrics.activeToday }}</span>
                <span class="metric-label">{{ 'admin.metrics.active_today' | translate }}</span>
              </div>
              <div class="metric-card">
                <span class="metric-value">{{ metrics.activeThisWeek }}</span>
                <span class="metric-label">{{ 'admin.metrics.active_week' | translate }}</span>
              </div>
              <div class="metric-card">
                <span class="metric-value">{{ metrics.newRegistrations }}</span>
                <span class="metric-label">{{ 'admin.metrics.new_users' | translate }}</span>
              </div>
              <div class="metric-card">
                <span class="metric-value">{{ metrics.totalCourses }}</span>
                <span class="metric-label">{{ 'admin.metrics.total_courses' | translate }}</span>
              </div>
              <div class="metric-card">
                <span class="metric-value">{{ metrics.totalBlocks }}</span>
                <span class="metric-label">{{ 'admin.metrics.total_blocks' | translate }}</span>
              </div>
              <div class="metric-card">
                <span class="metric-value">{{ metrics.totalForks }}</span>
                <span class="metric-label">{{ 'admin.metrics.total_forks' | translate }}</span>
              </div>
              <div class="metric-card">
                <span class="metric-value">{{ metrics.totalVersions }}</span>
                <span class="metric-label">{{ 'admin.metrics.total_versions' | translate }}</span>
              </div>
              <div class="metric-card">
                <span class="metric-value">{{ metrics.totalLikes }}</span>
                <span class="metric-label">{{ 'admin.metrics.total_likes' | translate }}</span>
              </div>
              <div class="metric-card">
                <span class="metric-value">{{ metrics.totalComments }}</span>
                <span class="metric-label">{{ 'admin.metrics.total_comments' | translate }}</span>
              </div>
              <div class="metric-card metric-card--accent">
                <span class="metric-value">{{ metrics.activeContests }}</span>
                <span class="metric-label">{{ 'admin.metrics.active_contests' | translate }}</span>
              </div>
              <div class="metric-card metric-card--accent">
                <span class="metric-value">{{ metrics.finishedContests }}</span>
                <span class="metric-label">{{ 'admin.metrics.finished_contests' | translate }}</span>
              </div>
            </div>

            @if (metrics.difficultyDistribution?.length) {
              <div class="admin-subsection">
                <h3>{{ 'admin.metrics.difficulty_distribution' | translate }}</h3>
                <div class="bar-chart">
                  @for (d of metrics.difficultyDistribution; track d.difficulty) {
                    <div class="bar-row">
                      <span class="bar-label">{{ d.difficulty }}</span>
                      <div class="bar-track">
                        <div class="bar-fill" [style.width.%]="barPercent(d.count)" [style.background]="difficultyColor(d.difficulty)"></div>
                      </div>
                      <span class="bar-count">{{ d.count }}</span>
                    </div>
                  }
                </div>
              </div>
            }

            @if (metrics.topTags?.length) {
              <div class="admin-subsection">
                <h3>{{ 'admin.metrics.top_tags' | translate }}</h3>
                <div class="tags-cloud">
                  @for (t of metrics.topTags; track t.tag) {
                    <span class="tag-badge" [style.font-size.px]="tagSize(t.count)">{{ t.tag }} ({{ t.count }})</span>
                  }
                </div>
              </div>
            }

            @if (metrics.topUsers?.length) {
              <div class="admin-subsection">
                <h3>{{ 'admin.metrics.top_users' | translate }}</h3>
                <table class="admin-table">
                  <thead>
                    <tr><th>{{ 'common.username' | translate }}</th><th>{{ 'common.points' | translate }}</th></tr>
                  </thead>
                  <tbody>
                    @for (u of metrics.topUsers; track u.userId) {
                      <tr>
                        <td>
                          <a [routerLink]="['/user', u.username]" class="user-link">{{ u.firstName }} {{ u.lastName }} (@{{ u.username }})</a>
                        </td>
                        <td class="points">{{ u.points }}</td>
                      </tr>
                    }
                  </tbody>
                </table>
              </div>
            }
          }
        </section>

        <!-- Active contests -->
        <section class="admin-section">
          <h2>{{ 'admin.manage_contests' | translate }}</h2>
          @if (contests.length > 0) {
            <table class="admin-table">
              <thead>
                <tr>
                  <th>{{ 'common.id' | translate }}</th>
                  <th>{{ 'common.title' | translate }}</th>
                  <th>{{ 'common.status' | translate }}</th>
                  <th>{{ 'contest.participants' | translate }}</th>
                  <th>{{ 'common.actions' | translate }}</th>
                </tr>
              </thead>
              <tbody>
                @for (c of contests; track c.id) {
                  <tr>
                    <td>{{ c.id }}</td>
                    <td><a [routerLink]="['/contest', c.id]">{{ c.title }}</a></td>
                    <td><span class="contest-badge contest-badge--{{ c.status.toLowerCase() }}">{{ c.status }}</span></td>
                    <td>{{ c.participantCount }}</td>
                    <td class="admin-actions">
                      <button class="btn btn--sm btn--primary" [routerLink]="['/admin/contests', c.id, 'edit']">{{ 'common.edit' | translate }}</button>
                      @if (c.status !== 'RESULTS' && c.status !== 'CANCELLED') {
                        <button class="btn btn--sm btn--danger" (click)="finishContest(c)">{{ 'contest.finish' | translate }}</button>
                      }
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          } @else {
            <p class="empty-state">{{ 'admin.no_contests' | translate }}</p>
          }
        </section>
      }
    </section>
  `,
  styles: [`
    .admin-page { max-width: 1000px; margin: 0 auto; padding: 1.5rem; }
    .admin-page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; }
    .admin-page-header h1 { margin: 0; color: var(--color-text-prim); }
    .admin-header-actions { display: flex; gap: 0.5rem; }
    .admin-access-denied { text-align: center; padding: 4rem; color: var(--color-text-secu); }
    .admin-access-denied i { font-size: 3rem; margin-bottom: 1rem; opacity: 0.4; }
    .admin-section { margin-bottom: 2rem; }
    .admin-section h2 { color: var(--color-text-prim); margin-bottom: 1rem; padding-bottom: 0.5rem; border-bottom: 1px solid var(--border); }
    .admin-subsection { margin-bottom: 1.5rem; }
    .admin-subsection h3 { font-size: 0.95rem; color: var(--color-text-secu); margin-bottom: 0.75rem; }
    .metrics-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(140px, 1fr)); gap: 0.75rem; margin-bottom: 1.5rem; }
    .metric-card { background: var(--color-bg); padding: 1rem; border-radius: 8px; box-shadow: 0 1px 3px var(--box-shadow-color); display: flex; flex-direction: column; gap: 0.25rem; }
    .metric-card--accent { border-left: 3px solid var(--color-primary); }
    .metric-value { font-size: 1.75rem; font-weight: 800; color: var(--color-text-prim); }
    .metric-label { font-size: 0.7rem; color: var(--color-text-secu); text-transform: uppercase; letter-spacing: 0.5px; }
    .admin-table { width: 100%; border-collapse: collapse; background: var(--color-bg); border-radius: 8px; overflow: hidden; box-shadow: 0 1px 3px var(--box-shadow-color); }
    .admin-table th { background: var(--input-background); padding: 0.65rem 0.75rem; text-align: left; color: var(--color-text-secu); font-weight: 500; font-size: 0.8rem; }
    .admin-table td { padding: 0.65rem 0.75rem; border-top: 1px solid var(--border); color: var(--color-text-prim); font-size: 0.9rem; }
    .admin-table .points { font-weight: 700; color: var(--color-primary); }
    .admin-actions { display: flex; gap: 0.3rem; }
    .user-link { color: var(--color-primary); text-decoration: none; }
    .user-link:hover { text-decoration: underline; }
    .empty-state { text-align: center; padding: 2rem; color: var(--color-text-secu); }
    .contest-badge { display: inline-block; padding: 0.15rem 0.5rem; border-radius: 4px; font-size: 0.7rem; font-weight: 700; text-transform: uppercase; }
    .contest-badge--announced, .contest-badge--preparation { background: rgba(33,150,243,0.12); color: #2196f3; }
    .contest-badge--building { background: rgba(255,193,7,0.12); color: #f57c00; }
    .contest-badge--validation { background: rgba(156,39,176,0.12); color: #9c27b0; }
    .contest-badge--results, .contest-badge--finished { background: rgba(0,191,99,0.12); color: #00bf63; }
    .bar-chart { display: flex; flex-direction: column; gap: 0.5rem; }
    .bar-row { display: flex; align-items: center; gap: 0.75rem; }
    .bar-label { width: 80px; font-size: 0.8rem; color: var(--color-text-secu); text-transform: capitalize; }
    .bar-track { flex: 1; height: 20px; background: var(--input-background); border-radius: 4px; overflow: hidden; }
    .bar-fill { height: 100%; border-radius: 4px; transition: width 0.5s; }
    .bar-count { width: 40px; font-size: 0.8rem; color: var(--color-text-secu); text-align: right; }
    .tags-cloud { display: flex; flex-wrap: wrap; gap: 0.5rem; }
    .tag-badge { padding: 0.3rem 0.6rem; background: var(--color-bg); border-radius: 6px; box-shadow: 0 1px 3px var(--box-shadow-color); color: var(--color-text-prim); }
    .btn { display: inline-block; padding: 0.5rem 1rem; border: none; border-radius: 4px; cursor: pointer; font-size: 0.85rem; text-decoration: none; text-align: center; transition: opacity 0.2s; }
    .btn:disabled { opacity: 0.5; cursor: not-allowed; }
    .btn--sm { padding: 0.3rem 0.6rem; font-size: 0.8rem; }
    .btn--primary { background: var(--color-primary); color: var(--color-text-btn); }
    .btn--danger { background: var(--color-error); color: #fff; }
  `]
})
export class AdminComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();
  private readonly authState = inject(AuthStateService);
  private readonly contestService = inject(ContestService);

  isAdmin = false;
  loading = true;
  metrics: DashboardMetricsDTO | null = null;
  contests: ContestResponseDTO[] = [];

  ngOnInit(): void {
    this.authState.user$.pipe(takeUntil(this.destroy$)).subscribe(u => {
      this.isAdmin = u?.role === 'ADMIN';
      if (this.isAdmin) {
        this.loadData();
      } else {
        this.loading = false;
      }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadData(): void {
    this.loading = true;
    this.contestService.getDashboard().pipe(takeUntil(this.destroy$)).subscribe({
      next: (m) => {
        this.metrics = m;
        this.loading = false;
      },
      error: () => this.loading = false,
    });
    this.contestService.list().pipe(takeUntil(this.destroy$)).subscribe({
      next: (page) => this.contests = page.content,
    });
  }

  barPercent(count: number): number {
    if (!this.metrics?.difficultyDistribution?.length) return 0;
    const max = Math.max(...this.metrics.difficultyDistribution.map(d => d.count));
    return max > 0 ? (count / max) * 100 : 0;
  }

  difficultyColor(difficulty: string): string {
    const colors: Record<string, string> = { BEGINNER: '#4caf50', INTERMEDIATE: '#ffc107', ADVANCED: '#ff9800', EXPERT: '#f44336' };
    return colors[difficulty] || '#9e9e9e';
  }

  tagSize(count: number): number {
    if (!this.metrics?.topTags?.length) return 12;
    const max = Math.max(...this.metrics.topTags.map(t => t.count));
    return max > 0 ? 12 + (count / max) * 14 : 12;
  }

  finishContest(c: ContestResponseDTO): void {
    if (confirm(`Finish contest "${c.title}"?`)) {
      this.contestService.finishContest(c.id).pipe(takeUntil(this.destroy$)).subscribe({
        next: () => this.loadData(),
      });
    }
  }
}
