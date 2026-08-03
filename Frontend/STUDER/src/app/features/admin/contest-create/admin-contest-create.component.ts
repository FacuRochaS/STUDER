import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { Subject, takeUntil } from 'rxjs';
import { ContestService } from '../../contest/contest.service';
import { ContestResponseDTO } from '../../contest/contest.model';
import { TextCreatorComponent } from '../../blocks/text/creator/text-creator.component';
import { TextContentData } from '../../blocks/interfaces/content.interfaces';
import { TagInputComponent } from '../../../shared/components/tag-input/tag-input.component';

@Component({
  selector: 'studer-admin-contest-create',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, TranslateModule, TextCreatorComponent, TagInputComponent],
  template: `
    <section class="cf">
      <div class="cf-card">
        <h1>{{ isEdit ? ('admin.edit_contest' | translate) : ('admin.create_contest' | translate) }}</h1>
        <div class="stepper-inline">
          <button class="step" [class.active]="step === 1" [class.done]="step > 1" (click)="step = 1">
            <span class="step__badge">{{ step > 1 ? '✓' : '1' }}</span>
            <span class="step__label">{{ 'contest.step_info' | translate }}</span>
          </button>
          <div class="step__line" [class.done]="step > 1"></div>
          <button class="step" [class.active]="step === 2" (click)="step = 2">
            <span class="step__badge">2</span>
            <span class="step__label">{{ 'contest.step_content' | translate }}</span>
          </button>
        </div>
        <form (ngSubmit)="submit()" class="form">
          @if (step === 1) {
            <div class="fg">
              <div class="f f--full"><label>{{ 'contest.title' | translate }} *</label><input [(ngModel)]="title" name="title" required class="inp" /></div>
              <div class="f f--full"><label>{{ 'contest.description' | translate }}</label><textarea [(ngModel)]="desc" name="desc" rows="2" class="inp"></textarea></div>
              <div class="f f--full"><label>{{ 'contest.tags' | translate }}</label><studer-tag-input [(tags)]="tags"></studer-tag-input></div>
              <div class="f"><label>{{ 'contest.start_date' | translate }} *</label><input type="datetime-local" [(ngModel)]="startDate" name="sd" required class="inp" /></div>
              <div class="f"><label>{{ 'admin.preparation_hours' | translate }}</label><input type="number" [(ngModel)]="prepH" name="ph" min="0" class="inp" placeholder="72" /></div>
              <div class="f"><label>{{ 'admin.validation_hours' | translate }}</label><input type="number" [(ngModel)]="valH" name="vh" min="0" class="inp" placeholder="120" /></div>
              <div class="f"><label>{{ 'contest.min_level' | translate }}</label><input type="number" [(ngModel)]="minLvl" name="ml" min="1" class="inp" placeholder="1" /></div>
            </div>
          }
          @if (step === 2) {
            <div class="f f--full"><label>{{ 'contest.content' | translate }}</label><studer-text-creator [data]="contentData" (dataChange)="contentData = $event"></studer-text-creator></div>
          }
          <div class="fa">
            <a routerLink="/admin" class="btn btn--outline">{{ 'common.cancel' | translate }}</a>
            <div class="fa-right">
              @if (step === 2) { <button type="button" class="btn btn--outline" (click)="step = 1"><i class="pi pi-arrow-left"></i> {{ 'course.create.back' | translate }}</button> }
              @if (step === 1) { <button type="button" class="btn btn--primary" (click)="step = 2" [disabled]="!title || !startDate">{{ 'course.create.next' | translate }} <i class="pi pi-arrow-right"></i></button> }
              @if (step === 2) { <button type="submit" class="btn btn--primary" [disabled]="saving || !title || !startDate">{{ saving ? ('common.saving' | translate) : (isEdit ? ('common.save' | translate) : ('common.create' | translate)) }}</button> }
            </div>
          </div>
        </form>
      </div>
    </section>
  `,
  styles: [`
    .cf { max-width: 720px; margin: 0 auto; padding: 1.5rem; }
    .cf-card { background: var(--color-bg); border-radius: 16px; padding: 1.5rem; box-shadow: 0 2px 12px var(--box-shadow-color); }
    .cf-card h1 { margin: 0 0 1rem; color: var(--color-text-prim); font-size: 1.25rem; }
    .stepper-inline { display: flex; align-items: center; justify-content: center; gap: 0; margin-bottom: 1.25rem; padding: 0.25rem 0; }
    .step { display: flex; align-items: center; gap: 0.5rem; background: none; border: none; cursor: pointer; padding: 0.4rem 0.75rem; border-radius: 6px; transition: background 0.2s; font-family: inherit; }
    .step:hover { background: var(--sidebar-hover-bg); }
    .step__badge { width: 26px; height: 26px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 0.75rem; background: var(--input-background); color: var(--color-text-secu); border: 2px solid var(--border); transition: all 0.3s; flex-shrink: 0; }
    .step.active .step__badge { background: var(--color-primary); color: var(--color-text-btn); border-color: var(--color-primary); }
    .step.done .step__badge { background: var(--color-correct); color: #fff; border-color: var(--color-correct); }
    .step__label { font-weight: 600; font-size: 0.85rem; color: var(--color-text-secu); }
    .step.active .step__label { color: var(--color-primary); }
    .step.done .step__label { color: var(--color-correct); }
    .step__line { width: 32px; height: 2px; background: var(--border); transition: background 0.3s; }
    .step__line.done { background: var(--color-correct); }
    .form { display: flex; flex-direction: column; gap: 1rem; }
    .fg { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
    .f--full { grid-column: 1/-1; }
    .f { display: flex; flex-direction: column; gap: 0.3rem; }
    .f label { color: var(--color-text-secu); font-size: 0.8rem; font-weight: 600; }
    .inp { padding: 0.5rem 0.7rem; border: 1px solid var(--border); border-radius: 8px; background: var(--input-background); color: var(--color-text-prim); font-family: inherit; font-size: 0.88rem; }
    .inp:focus { outline: none; border-color: var(--color-primary); }
    .fa { display: flex; justify-content: space-between; align-items: center; margin-top: 0.25rem; }
    .fa-right { display: flex; gap: 0.5rem; }
    .btn { display: inline-flex; align-items: center; gap: 0.4rem; padding: 0.55rem 1.2rem; border: none; border-radius: 8px; cursor: pointer; font-size: 0.85rem; font-weight: 600; text-decoration: none; font-family: inherit; transition: opacity 0.2s; }
    .btn:disabled { opacity: 0.4; cursor: not-allowed; }
    .btn--primary { background: var(--color-primary); color: var(--color-text-btn); }
    .btn--primary:hover:not(:disabled) { opacity: 0.9; }
    .btn--outline { background: transparent; color: var(--color-text-secu); border: 1px solid var(--border); }
    .btn--outline:hover { color: var(--color-primary); border-color: var(--color-primary); }
    @media (max-width: 600px) { .cf { padding: 1rem; } .cf-card { padding: 1rem; } .fg { grid-template-columns: 1fr; } }
  `]
})
export class AdminContestCreateComponent implements OnInit {
  private destroy$ = new Subject<void>();
  private cs = inject(ContestService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  isEdit = false; editId: number | null = null; saving = false; step = 1;
  title = ''; desc = ''; tags: string[] = []; startDate = ''; prepH = 72; valH = 120; minLvl: number | null = null;
  contentData: TextContentData = { paragraphs: [] };

  ngOnInit(): void {
    this.route.paramMap.pipe(takeUntil(this.destroy$)).subscribe(p => {
      const id = p.get('id');
      if (id) { this.isEdit = true; this.editId = +id; this.cs.getById(this.editId).pipe(takeUntil(this.destroy$)).subscribe(c => this.pop(c)); }
    });
  }

  pop(c: ContestResponseDTO): void {
    this.title = c.title; this.desc = c.description || ''; this.minLvl = pointsToLevel(c.minPoints);
    this.tags = c.tags || []; this.prepH = c.preparationDurationHours || 72; this.valH = c.validationDurationHours || 120;
    if (c.startDate) this.startDate = toLocal(c.startDate);
    if (c.content?.paragraphs) this.contentData = c.content as TextContentData;
  }

  submit(): void {
    if (!this.title || !this.startDate) return;
    this.saving = true;
    const p: any = {
      title: this.title, description: this.desc || undefined,
      tags: this.tags.length > 0 ? this.tags : undefined,
      content: this.contentData.paragraphs.length > 0 ? JSON.stringify(this.contentData) : undefined,
      startDate: this.startDate ? this.startDate + ':00' : undefined,
      preparationDurationHours: this.prepH,
      validationDurationHours: this.valH,
      minPoints: this.minLvl != null ? levelToPoints(this.minLvl) : undefined,
    };
    const obs = this.isEdit && this.editId ? this.cs.updateContest(this.editId, p) : this.cs.createContest(p);
    obs.subscribe({ next: () => this.router.navigate(['/admin']), error: () => this.saving = false });
  }
}
function toLocal(iso: string): string { if (!iso) return ''; const d = new Date(iso); const p = (n: number) => String(n).padStart(2, '0'); return `${d.getFullYear()}-${p(d.getMonth()+1)}-${p(d.getDate())}T${p(d.getHours())}:${p(d.getMinutes())}`; }

const LVL_THRESHOLDS = [0, 10, 20, 40, 80, 100, 200, 300, 500, 700, 1000, 2000, 4000, 8000, 16000];

function levelToPoints(level: number): number {
  if (level <= 1) return 0;
  return LVL_THRESHOLDS[Math.min(level - 1, LVL_THRESHOLDS.length - 1)] ?? 0;
}

function pointsToLevel(points: number | null): number | null {
  if (points == null || points < 0) return null;
  for (let i = LVL_THRESHOLDS.length - 1; i >= 0; i--) {
    if (points >= LVL_THRESHOLDS[i]) return i + 1;
  }
  return 1;
}
