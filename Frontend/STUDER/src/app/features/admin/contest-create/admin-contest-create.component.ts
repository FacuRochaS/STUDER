import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { Subject, switchMap, takeUntil } from 'rxjs';
import { ContestService } from '../../contest/contest.service';
import { ContestResponseDTO } from '../../contest/contest.model';

@Component({
  selector: 'studer-admin-contest-create',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, TranslateModule],
  template: `
    <section class="admin-contest-form">
      <h1>{{ isEdit ? ('admin.edit_contest' | translate) : ('admin.create_contest' | translate) }}</h1>

      <form (ngSubmit)="onSubmit()" class="form">
        <div class="form-grid">
          <div class="field">
            <label>{{ 'contest.title' | translate }} *</label>
            <input type="text" [(ngModel)]="title" name="title" required />
          </div>

          <div class="field">
            <label>{{ 'contest.banner' | translate }}</label>
            <input type="text" [(ngModel)]="banner" name="banner" placeholder="https://..." />
          </div>

          <div class="field field--full">
            <label>{{ 'contest.description' | translate }}</label>
            <textarea [(ngModel)]="description" name="description" rows="3"></textarea>
          </div>

          <div class="field">
            <label>{{ 'contest.theme' | translate }}</label>
            <input type="text" [(ngModel)]="theme" name="theme" />
          </div>

          <div class="field">
            <label>{{ 'contest.difficulty' | translate }}</label>
            <select [(ngModel)]="difficulty" name="difficulty">
              <option value="">--</option>
              <option value="BEGINNER">BEGINNER</option>
              <option value="INTERMEDIATE">INTERMEDIATE</option>
              <option value="ADVANCED">ADVANCED</option>
              <option value="EXPERT">EXPERT</option>
            </select>
          </div>

          <div class="field">
            <label>{{ 'contest.tags' | translate }}</label>
            <input type="text" [(ngModel)]="tagsInput" name="tags" placeholder="tag1, tag2, tag3" />
          </div>

          <div class="field">
            <label>{{ 'contest.start_date' | translate }} *</label>
            <input type="datetime-local" [(ngModel)]="startDate" name="startDate" required />
          </div>

          <div class="field">
            <label>{{ 'contest.preparation_end' | translate }}</label>
            <input type="datetime-local" [(ngModel)]="preparationEndDate" name="preparationEndDate" />
          </div>

          <div class="field">
            <label>{{ 'contest.building_end' | translate }}</label>
            <input type="datetime-local" [(ngModel)]="buildingEndDate" name="buildingEndDate" />
          </div>

          <div class="field">
            <label>{{ 'contest.validation_end' | translate }}</label>
            <input type="datetime-local" [(ngModel)]="validationEndDate" name="validationEndDate" />
          </div>

          <div class="field">
            <label>{{ 'contest.end_date' | translate }} *</label>
            <input type="datetime-local" [(ngModel)]="endDate" name="endDate" required />
          </div>

          <div class="field">
            <label>{{ 'contest.min_level' | translate }}</label>
            <input type="number" [(ngModel)]="minLevel" name="minLevel" min="0" />
          </div>

          <div class="field">
            <label>{{ 'contest.min_reputation' | translate }}</label>
            <input type="number" [(ngModel)]="minReputation" name="minReputation" min="0" />
          </div>

          <div class="field">
            <label>{{ 'contest.max_participants' | translate }}</label>
            <input type="number" [(ngModel)]="maxParticipants" name="maxParticipants" min="0" />
          </div>

          <div class="field field--full">
            <label>{{ 'contest.external_links' | translate }}</label>
            <textarea [(ngModel)]="externalLinks" name="externalLinks" rows="2" placeholder="https://..."></textarea>
          </div>

          <div class="field field--full">
            <label>{{ 'contest.bibliography' | translate }}</label>
            <textarea [(ngModel)]="bibliography" name="bibliography" rows="2"></textarea>
          </div>

          <div class="field field--full">
            <label>{{ 'contest.learning_objectives' | translate }}</label>
            <textarea [(ngModel)]="learningObjectives" name="learningObjectives" rows="2"></textarea>
          </div>
        </div>

        <div class="form-actions">
          <button type="submit" class="btn btn--primary" [disabled]="submitting">
            {{ submitting ? ('common.saving' | translate) : (isEdit ? ('common.save' | translate) : ('common.create' | translate)) }}
          </button>
          <a routerLink="/admin" class="btn btn--secondary">{{ 'common.cancel' | translate }}</a>
        </div>
      </form>
    </section>
  `,
  styles: [`
    .admin-contest-form { max-width: 700px; margin: 0 auto; padding: 1.5rem; }
    .admin-contest-form h1 { color: var(--color-text-prim); margin-bottom: 1.5rem; }
    .form { display: flex; flex-direction: column; gap: 1.25rem; }
    .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
    .field--full { grid-column: 1 / -1; }
    .field { display: flex; flex-direction: column; gap: 0.3rem; }
    .field label { color: var(--color-text-secu); font-size: 0.85rem; font-weight: 500; }
    .field input, .field textarea, .field select { padding: 0.5rem; border: 1px solid var(--border); border-radius: 4px; background: var(--input-background); color: var(--color-text-prim); font-family: inherit; font-size: 0.9rem; }
    .field input:focus, .field textarea:focus, .field select:focus { outline: none; box-shadow: 0 0 0 2px rgba(66,133,244,0.2); }
    .form-actions { display: flex; gap: 0.5rem; margin-top: 0.5rem; }
    .btn { padding: 0.5rem 1rem; border: none; border-radius: 4px; cursor: pointer; font-size: 0.9rem; text-decoration: none; text-align: center; }
    .btn:disabled { opacity: 0.5; cursor: not-allowed; }
    .btn--primary { background: var(--color-primary); color: var(--color-text-btn); }
    .btn--secondary { background: transparent; border: 1px solid var(--border); color: var(--color-text-secu); }
  `]
})
export class AdminContestCreateComponent implements OnInit {
  private readonly destroy$ = new Subject<void>();
  private contestService = inject(ContestService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  isEdit = false;
  editId: number | null = null;
  submitting = false;

  title = '';
  banner = '';
  description = '';
  theme = '';
  difficulty = '';
  tagsInput = '';
  startDate = '';
  preparationEndDate = '';
  buildingEndDate = '';
  validationEndDate = '';
  endDate = '';
  externalLinks = '';
  bibliography = '';
  learningObjectives = '';
  minLevel: number | null = null;
  minReputation: number | null = null;
  maxParticipants: number | null = null;

  ngOnInit(): void {
    this.route.paramMap.pipe(takeUntil(this.destroy$)).subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.isEdit = true;
        this.editId = +id;
        this.contestService.getById(this.editId).pipe(takeUntil(this.destroy$)).subscribe(c => this.populateForm(c));
      }
    });
  }

  private populateForm(c: ContestResponseDTO): void {
    this.title = c.title;
    this.banner = c.banner || '';
    this.description = c.description || '';
    this.theme = c.theme || '';
    this.difficulty = c.difficulty || '';
    this.tagsInput = c.tags?.join(', ') || '';
    this.startDate = toDatetimeLocal(c.startDate);
    this.preparationEndDate = c.preparationEndDate ? toDatetimeLocal(c.preparationEndDate) : '';
    this.buildingEndDate = c.buildingEndDate ? toDatetimeLocal(c.buildingEndDate) : '';
    this.validationEndDate = c.validationEndDate ? toDatetimeLocal(c.validationEndDate) : '';
    this.endDate = toDatetimeLocal(c.endDate);
    this.externalLinks = c.externalLinks || '';
    this.bibliography = c.bibliography || '';
    this.learningObjectives = c.learningObjectives || '';
    this.minLevel = c.minLevel ?? null;
    this.minReputation = c.minReputation ?? null;
    this.maxParticipants = c.maxParticipants ?? null;
  }

  onSubmit(): void {
    if (!this.title || !this.startDate || !this.endDate) return;
    this.submitting = true;
    const tags = this.tagsInput.split(',').map(t => t.trim()).filter(Boolean);
    const payload = {
      title: this.title,
      banner: this.banner || undefined,
      description: this.description || undefined,
      theme: this.theme || undefined,
      difficulty: this.difficulty || undefined,
      content: { consigna: this.description || '' },
      tags: tags.length > 0 ? tags : undefined,
      externalLinks: this.externalLinks || undefined,
      bibliography: this.bibliography || undefined,
      learningObjectives: this.learningObjectives || undefined,
      minLevel: this.minLevel ?? undefined,
      minReputation: this.minReputation ?? undefined,
      maxParticipants: this.maxParticipants ?? undefined,
      startDate: new Date(this.startDate).toISOString(),
      preparationEndDate: this.preparationEndDate ? new Date(this.preparationEndDate).toISOString() : undefined,
      buildingEndDate: this.buildingEndDate ? new Date(this.buildingEndDate).toISOString() : undefined,
      validationEndDate: this.validationEndDate ? new Date(this.validationEndDate).toISOString() : undefined,
      endDate: new Date(this.endDate).toISOString(),
    };

    const obs = this.isEdit && this.editId
      ? this.contestService.updateContest(this.editId, payload)
      : this.contestService.createContest(payload);

    obs.subscribe({
      next: () => this.router.navigate(['/admin']),
      error: () => this.submitting = false,
    });
  }
}

function toDatetimeLocal(iso: string): string {
  if (!iso) return '';
  const d = new Date(iso);
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}
