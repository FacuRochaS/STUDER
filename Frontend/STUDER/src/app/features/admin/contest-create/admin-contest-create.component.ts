import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { ContestService } from '../../contest/contest.service';

@Component({
  selector: 'studer-admin-contest-create',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, TranslateModule],
  template: `
    <section class="admin-contest-create">
      <h1>{{ 'admin.create_contest' | translate }}</h1>
      <form (ngSubmit)="onSubmit()" class="form">
        <div class="field">
          <label>{{ 'contest.title' | translate }}</label>
          <input type="text" [(ngModel)]="title" name="title" required />
        </div>
        <div class="field">
          <label>{{ 'contest.consigna' | translate }}</label>
          <textarea [(ngModel)]="consigna" name="consigna" rows="5" required></textarea>
        </div>
        <div class="field">
          <label>{{ 'contest.tags' | translate }}</label>
          <input type="text" [(ngModel)]="tagsInput" name="tags" placeholder="tag1, tag2, tag3" />
        </div>
        <div class="field">
          <label>{{ 'contest.start_date' | translate }}</label>
          <input type="datetime-local" [(ngModel)]="startDate" name="startDate" required />
        </div>
        <div class="form-actions">
          <button type="submit" class="btn btn--primary" [disabled]="submitting">
            {{ 'common.create' | translate }}
          </button>
          <a routerLink="/admin" class="btn btn--secondary">{{ 'common.cancel' | translate }}</a>
        </div>
      </form>
    </section>
  `,
  styles: [`
    .admin-contest-create { max-width: 600px; margin: 0 auto; padding: 1.5rem; }
    .admin-contest-create h1 { color: var(--color-text-prim); margin-bottom: 1.5rem; }
    .form { display: flex; flex-direction: column; gap: 1rem; }
    .field { display: flex; flex-direction: column; gap: 0.3rem; }
    .field label { color: var(--color-text-secu); font-size: 0.9rem; }
    .field input, .field textarea { padding: 0.5rem; border: 1px solid var(--border); border-radius: 4px; background: var(--input-background); color: var(--color-text-prim); font-family: inherit; }
    .form-actions { display: flex; gap: 0.5rem; margin-top: 0.5rem; }
    .btn { padding: 0.5rem 1rem; border: none; border-radius: 4px; cursor: pointer; font-size: 0.9rem; text-decoration: none; text-align: center; }
    .btn:disabled { opacity: 0.5; cursor: not-allowed; }
    .btn--primary { background: var(--color-primary); color: var(--color-text-btn); }
    .btn--secondary { background: transparent; border: 1px solid var(--border); color: var(--color-text-secu); }
  `]
})
export class AdminContestCreateComponent {
  private contestService = inject(ContestService);
  private router = inject(Router);

  title = '';
  consigna = '';
  tagsInput = '';
  startDate = '';
  submitting = false;

  onSubmit(): void {
    if (!this.title || !this.consigna || !this.startDate) return;
    this.submitting = true;
    const tags = this.tagsInput.split(',').map(t => t.trim()).filter(Boolean);
    this.contestService.createContest({
      title: this.title,
      content: { consigna: this.consigna },
      tags,
      startDate: new Date(this.startDate).toISOString()
    }).subscribe({
      next: () => this.router.navigate(['/admin']),
      error: () => this.submitting = false
    });
  }
}
