import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { ContestService } from '../../contest.service';
import { ContestResponseDTO } from '../../contest.model';
import { LoaderComponent } from '../../../../shared/components/loader/loader.component';

@Component({
  selector: 'studer-contest-list',
  standalone: true,
  imports: [CommonModule, TranslateModule, RouterLink, LoaderComponent],
  template: `
    <div class="contest-list-page">
      <header class="contest-list-header">
        <h1>{{ 'contest.list.title' | translate }}</h1>
        <p class="contest-list-subtitle">{{ 'contest.list.subtitle' | translate }}</p>
      </header>

      @if (loading) {
        <studer-loader></studer-loader>
      } @else {
        @if (activeContests.length > 0) {
          <section class="contest-section">
            <h2 class="contest-section-title">{{ 'contest.list.active' | translate }}</h2>
            <div class="contest-grid">
              @for (c of activeContests; track c.id) {
                <a class="contest-card contest-card--active" [routerLink]="['/contest', c.id]">
                  <div class="contest-card-body">
                    <span class="contest-badge contest-badge--{{ 'c.status' | translate }}">{{ getStatusLabel(c.status) }}</span>
                    <h3 class="contest-card-title">{{ c.title }}</h3>
                    @if (c.description) {
                      <p class="contest-card-desc">{{ c.description }}</p>
                    }
                    @if (c.tags?.length) {
                      <div class="contest-card-tags">@for (t of c.tags; track t) { <span class="entity entity--tag">#{{ t }}</span> }</div>
                    }
                    <div class="contest-card-meta">
                      <span><i class="pi pi-users"></i> {{ c.participantCount }}</span>
                      <span><i class="pi pi-book"></i> {{ c.courseCount }}</span>
                      <span><i class="pi pi-calendar"></i> {{ c.startDate | date:'shortDate' }}</span>
                    </div>
                  </div>
                </a>
              }
            </div>
          </section>
        }

        @if (upcomingContests.length > 0) {
          <section class="contest-section">
            <h2 class="contest-section-title">{{ 'contest.list.upcoming' | translate }}</h2>
            <div class="contest-grid">
              @for (c of upcomingContests; track c.id) {
                <a class="contest-card" [routerLink]="['/contest', c.id]">
                  <div class="contest-card-body">
                    <span class="contest-badge contest-badge--announced">{{ 'contest.status.announced' | translate }}</span>
                    <h3 class="contest-card-title">{{ c.title }}</h3>
                    @if (c.description) {
                      <p class="contest-card-desc">{{ c.description }}</p>
                    }
                    @if (c.tags?.length) {
                      <div class="contest-card-tags">@for (t of c.tags; track t) { <span class="entity entity--tag">#{{ t }}</span> }</div>
                    }
                    <div class="contest-card-meta">
                      <span><i class="pi pi-calendar"></i> {{ 'contest.list.starts' | translate }} {{ c.startDate | date:'shortDate' }}</span>
                    </div>
                  </div>
                </a>
              }
            </div>
          </section>
        }

        @if (finishedContests.length > 0) {
          <section class="contest-section">
            <h2 class="contest-section-title">{{ 'contest.list.finished' | translate }}</h2>
            <div class="contest-grid">
              @for (c of finishedContests; track c.id) {
                <a class="contest-card contest-card--finished" [routerLink]="['/contest', c.id]">
                  <div class="contest-card-body">
                    <span class="contest-badge contest-badge--results">{{ 'contest.status.results' | translate }}</span>
                    <h3 class="contest-card-title">{{ c.title }}</h3>
                    @if (c.description) {
                      <p class="contest-card-desc">{{ c.description }}</p>
                    }
                    @if (c.tags?.length) {
                      <div class="contest-card-tags">@for (t of c.tags; track t) { <span class="entity entity--tag">#{{ t }}</span> }</div>
                    }
                    <div class="contest-card-meta">
                      <span><i class="pi pi-users"></i> {{ c.participantCount }}</span>
                      <span><i class="pi pi-trophy"></i> {{ c.courseCount }}</span>
                    </div>
                  </div>
                </a>
              }
            </div>
          </section>
        }

        @if (activeContests.length === 0 && upcomingContests.length === 0 && finishedContests.length === 0) {
          <div class="contest-empty">
            <i class="pi pi-list-check"></i>
            <p>{{ 'contest.list.empty' | translate }}</p>
          </div>
        }
      }
    </div>
  `,
  styles: [`
    :host { display: block; padding: 1.5rem; max-width: 900px; margin: 0 auto; }
    .contest-list-header { margin-bottom: 2rem; }
    .contest-list-header h1 { margin: 0 0 0.5rem; font-size: 1.75rem; color: var(--color-text-prim); }
    .contest-list-subtitle { margin: 0; color: var(--color-text-secu); }
    .contest-section { margin-bottom: 2rem; }
    .contest-section-title { font-size: 1.1rem; font-weight: 700; color: var(--color-text-secu); text-transform: uppercase; letter-spacing: 0.5px; margin: 0 0 1rem; padding-bottom: 0.5rem; border-bottom: 1px solid var(--border); }
    .contest-grid { display: grid; gap: 1.25rem; grid-template-columns: repeat(auto-fill, minmax(360px, 1fr)); }
    .contest-card { display: flex; flex-direction: column; background: var(--color-bg); border-radius: 14px; overflow: hidden; box-shadow: 0 1px 4px var(--box-shadow-color); text-decoration: none; transition: box-shadow 0.25s, transform 0.2s; cursor: pointer; min-height: 160px; border: 1px solid var(--border); }
    .contest-card:hover { box-shadow: 0 4px 20px var(--box-shadow-color); transform: translateY(-3px); }
    .contest-card--active { border-left: 4px solid var(--color-primary); }
    .contest-card--finished { opacity: 0.8; border-left: 4px solid var(--color-correct); }
    .contest-card-body { padding: 1.25rem; flex: 1; display: flex; flex-direction: column; gap: 0.6rem; }
    .contest-card-title { margin: 0; font-size: 1rem; font-weight: 700; color: var(--color-text-prim); }
    .contest-card-desc { margin: 0; font-size: 0.85rem; color: var(--color-text-secu); line-height: 1.4; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
    .contest-card-tags { display: flex; gap: 0.3rem; flex-wrap: wrap; }
    .contest-card-meta { display: flex; gap: 1rem; font-size: 0.8rem; color: var(--color-text-secu); margin-top: auto; padding-top: 0.5rem; border-top: 1px solid var(--border); }
    .contest-card-meta span { display: flex; align-items: center; gap: 0.3rem; }
    .contest-badge { display: inline-flex; align-items: center; padding: 0.2rem 0.65rem; border-radius: 20px; font-size: 0.64rem; font-weight: 700; text-transform: uppercase; letter-spacing: 0.3px; width: fit-content; }
    .contest-badge--announced, .contest-badge--preparation { background: rgba(33,150,243,0.12); color: #2196f3; }
    .contest-badge--building { background: rgba(255,193,7,0.12); color: #ffc107; }
    .contest-badge--validation { background: rgba(156,39,176,0.12); color: #9c27b0; }
    .contest-badge--results { background: rgba(0,191,99,0.12); color: #00bf63; }
    .contest-empty { text-align: center; padding: 4rem 2rem; color: var(--color-text-secu); }
    .contest-empty i { font-size: 3rem; opacity: 0.3; margin-bottom: 1rem; display: block; }
    .contest-empty p { margin: 0; }
  `]
})
export class ContestListComponent implements OnInit {
  private contestService = inject(ContestService);
  loading = true;
  activeContests: ContestResponseDTO[] = [];
  upcomingContests: ContestResponseDTO[] = [];
  finishedContests: ContestResponseDTO[] = [];

  ngOnInit(): void {
    this.contestService.list().subscribe({
      next: (page) => {
        const all = page.content as ContestResponseDTO[];
        this.activeContests = all.filter(c => !['RESULTS', 'CANCELLED', 'ANNOUNCED'].includes(c.status));
        this.upcomingContests = all.filter(c => c.status === 'ANNOUNCED');
        this.finishedContests = all.filter(c => ['RESULTS', 'CANCELLED'].includes(c.status));
        this.loading = false;
      },
      error: () => this.loading = false,
    });
  }

  getStatusLabel(status: string): string {
    const map: Record<string, string> = {
      ANNOUNCED: 'contest.status.announced',
      PREPARATION: 'contest.status.preparation',
      VALIDATION: 'contest.status.validation',
      RESULTS: 'contest.status.results',
    };
    return map[status] || status;
  }
}
