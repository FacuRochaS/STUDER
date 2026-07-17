import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { Subject, switchMap, takeUntil } from 'rxjs';
import { ContestService } from '../../contest.service';
import { ContestResponseDTO, LeaderboardEntryDTO } from '../../contest.model';
import { AuthStateService } from '../../../../core/auth/auth-state.service';
import { LoaderComponent } from '../../../../shared/components/loader/loader.component';
import { RichTextComponent } from '../../../../shared/components/rich-text/rich-text.component';
import { TagComponent } from '../../../../shared/components/tag/tag.component';
import { RelativeTimePipe } from '../../../../shared/pipes/relative-time.pipe';
import { CourseCreateComponent } from '../../../courses/components/course-create/course-create.component';

@Component({
  selector: 'studer-contest-detail',
  standalone: true,
  imports: [
    CommonModule, FormsModule, RouterModule, TranslateModule,
    LoaderComponent, RichTextComponent, TagComponent, RelativeTimePipe, CourseCreateComponent
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
  loading = true;
  currentUserId: number | null = null;
  currentUserRole: string | null = null;
  error = '';

  showValidation = false;
  validatingCourse: any = null;
  userRating = 0;
  submittingRating = false;

  showSubmitForm = false;
  submittingCourse = false;

  ngOnInit(): void {
    this.authState.user$.pipe(takeUntil(this.destroy$)).subscribe((u: any) => {
      this.currentUserId = u?.id ?? null;
      this.currentUserRole = u?.role ?? null;
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
    this.contestService.getLeaderboard(this.contest.id).pipe(takeUntil(this.destroy$)).subscribe({
      next: (lb) => this.leaderboard = lb
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
    if (!this.contest || this.currentUserRole !== 'ADMIN') return;
    if (confirm('Finish this contest?')) {
      this.contestService.finishContest(this.contest.id).pipe(takeUntil(this.destroy$)).subscribe({
        next: () => { this.contest!.status = 'FINISHED'; this.loadLeaderboard(); }
      });
    }
  }
}
