import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { Subject, switchMap, takeUntil } from 'rxjs';
import { ContestService } from '../../contest.service';
import { ContestResponseDTO, LeaderboardEntryDTO, AchievementResponseDTO } from '../../contest.model';
import { AuthStateService } from '../../../../core/auth/auth-state.service';
import { LoaderComponent } from '../../../../shared/components/loader/loader.component';
import { TagComponent } from '../../../../shared/components/tag/tag.component';
import { CourseCreateComponent } from '../../../courses/components/course-create/course-create.component';
import { BlockViewerComponent } from '../../../blocks/component/block-viewer/block-viewer.component';
import { TextViewerComponent } from '../../../blocks/text/viewer/text-viewer.component';
import { BlockContentItem, TextContentData } from '../../../blocks/interfaces/content.interfaces';

@Component({
  selector: 'studer-contest-detail',
  standalone: true,
  imports: [
    CommonModule, FormsModule, RouterModule, TranslateModule,
    LoaderComponent, TagComponent, CourseCreateComponent, BlockViewerComponent, TextViewerComponent
  ],
  templateUrl: './contest-detail.component.html',
  styleUrls: ['./contest-detail.component.css']
})
export class ContestDetailComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();
  private route = inject(ActivatedRoute);
  private contestService = inject(ContestService);
  private authState = inject(AuthStateService);

  contest: ContestResponseDTO | null = null;
  leaderboard: LeaderboardEntryDTO[] = [];
  achievements: AchievementResponseDTO[] = [];
  loading = true;
  currentUserId: number | null = null;
  currentUserRole: string | null = null;
  currentUserPoints: number = 0;
  error = '';

  showValidation = false;
  validatingCourse: any = null;
  userRating = 0;
  submittingRating = false;

  showSubmitForm = false;
  activeTab = 'info';

  get isAdmin(): boolean { return this.currentUserRole === 'ADMIN'; }
  get hasMinLevel(): boolean {
    if (!this.contest?.minPoints) return true;
    return this.currentUserPoints >= this.contest.minPoints;
  }
  get canSubmit(): boolean {
    return this.contest?.status === 'PREPARATION' && this.hasMinLevel;
  }
  get canValidate(): boolean { return this.contest?.status === 'VALIDATION'; }

  get minLevel(): number | null {
    if (!this.contest?.minPoints) return null;
    return pointsToLevel(this.contest.minPoints);
  }

  phases = ['ANNOUNCED', 'PREPARATION', 'VALIDATION', 'RESULTS'];

  get phaseIndex(): number {
    return this.phases.indexOf(this.contest?.status ?? '');
  }

  get parsedContent(): any {
    if (!this.contest?.content) return null;
    if (typeof this.contest.content === 'string') {
      try { return JSON.parse(this.contest.content); } catch { return null; }
    }
    return this.contest.content;
  }

  get textContent(): TextContentData | null {
    const pc = this.parsedContent;
    if (!pc) return null;
    if (pc.paragraphs) return pc as TextContentData;
    if (pc.text) return { paragraphs: [{ align: 'left' as const, runs: [{ text: pc.text, imageUrl: null, link: null, bold: false, italic: false, underline: false, strikethrough: false, size: 'medium' as const, color: 'primary' as const }] }] };
    return null;
  }

  ngOnInit(): void {
    this.authState.user$.pipe(takeUntil(this.destroy$)).subscribe((u: any) => {
      this.currentUserId = u?.id ?? null;
      this.currentUserRole = u?.role ?? null;
      this.currentUserPoints = u?.points ?? 0;
    });

    this.route.paramMap.pipe(
      takeUntil(this.destroy$),
      switchMap(params => {
        const id = params.get('id');
        if (!id) { this.error = 'Invalid contest ID'; this.loading = false; return []; }
        return this.contestService.getById(+id);
      })
    ).subscribe({
      next: (contest) => {
        this.contest = contest;
        this.loading = false;
        this.loadLeaderboard();
        this.loadAchievements();
      },
      error: () => { this.error = 'Error loading contest'; this.loading = false; }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadLeaderboard(): void {
    if (!this.contest) return;
    this.contestService.getLeaderboard(this.contest.id).pipe(takeUntil(this.destroy$)).subscribe({ next: (lb) => this.leaderboard = lb });
  }

  loadContest(): void {
    if (!this.contest) return;
    this.contestService.getById(this.contest.id).pipe(takeUntil(this.destroy$)).subscribe({ next: (c) => { this.contest = c; this.loadLeaderboard(); } });
  }

  loadAchievements(): void {
    this.contestService.getAchievements().pipe(takeUntil(this.destroy$)).subscribe({
      next: (a) => this.achievements = a
    });
  }

  getRandomCourse(): void {
    if (!this.contest) return;
    this.contestService.getRandomValidationCourse(this.contest.id).pipe(takeUntil(this.destroy$)).subscribe({
      next: (course) => {
        this.validatingCourse = course;
        this.showValidation = true;
      }
    });
  }

  submitRating(): void {
    if (!this.validatingCourse || this.userRating < 1 || this.userRating > 5) return;
    this.submittingRating = true;
    this.contestService.rateCourse({ courseId: this.validatingCourse.id, rating: this.userRating }).pipe(takeUntil(this.destroy$)).subscribe({
      next: () => {
        this.submittingRating = false;
        this.userRating = 0;
        this.validatingCourse = null;
        this.showValidation = false;
        this.loadLeaderboard();
      },
      error: () => this.submittingRating = false
    });
  }

  finishContest(): void {
    if (!this.contest || !this.isAdmin) return;
    if (confirm('Finish this contest?')) {
      this.contestService.finishContest(this.contest.id).pipe(takeUntil(this.destroy$)).subscribe({
        next: () => { this.contest!.status = 'RESULTS'; this.loadLeaderboard(); }
      });
    }
  }

  changeStatus(newStatus: string): void {
    if (!this.contest || !this.isAdmin) return;
    this.contestService.changeStatus(this.contest.id, newStatus).pipe(takeUntil(this.destroy$)).subscribe({
      next: () => { this.contest!.status = newStatus; }
    });
  }

  getNextStatuses(): string[] {
    if (!this.contest) return [];
    const map: Record<string, string[]> = {
      ANNOUNCED: ['PREPARATION', 'CANCELLED'],
      PREPARATION: ['VALIDATION', 'CANCELLED'],
      VALIDATION: ['RESULTS', 'CANCELLED'],
      RESULTS: [],
      CANCELLED: ['ANNOUNCED'],
    };
    return map[this.contest.status] || [];
  }

  getPhaseProgress(): number {
    if (!this.contest) return 0;
    const idx = this.phases.indexOf(this.contest.status);
    return idx >= 0 ? ((idx + 1) / this.phases.length) * 100 : 0;
  }

getStatusLabel(status: string): string {
    const labels: Record<string, string> = {
      ANNOUNCED: 'contest.status.announced',
      PREPARATION: 'contest.status.preparation',
      VALIDATION: 'contest.status.validation',
      RESULTS: 'contest.status.results',
    };
    return labels[status] || status;
  }

  parseBlockContent(content: string): BlockContentItem[] {
    if (!content) return [];
    try { return JSON.parse(content); } catch { return []; }
  }

  getStatusBadgeClass(): string {
    if (!this.contest) return '';
    return `contest-badge--${this.contest.status.toLowerCase()}`;
  }

  daysRemaining(dateStr: string): number {
    if (!dateStr) return 0;
    const diff = new Date(dateStr).getTime() - Date.now();
    return Math.max(0, Math.ceil(diff / (1000 * 60 * 60 * 24)));
  }

  isBefore(dateStr: string): boolean {
    return !!dateStr && new Date(dateStr).getTime() > Date.now();
  }

  isAfter(dateStr: string): boolean {
    return !!dateStr && new Date(dateStr).getTime() < Date.now();
  }
}

const LVL = [0, 10, 20, 40, 80, 100, 200, 300, 500, 700, 1000, 2000, 4000, 8000, 16000];
function pointsToLevel(pts: number): number {
  for (let i = LVL.length - 1; i >= 0; i--) { if (pts >= LVL[i]) return i + 1; }
  return 1;
}
