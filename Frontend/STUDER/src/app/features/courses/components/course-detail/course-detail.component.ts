import { Component, OnDestroy, OnInit, inject, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { Subject, takeUntil, switchMap } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';
import { CourseService } from '../../course.service';
import { CourseResponseDTO, UserCourseBlockRequestDTO, UserCourseBlockResponseDTO } from '../../course.model';
import { BlockService } from '../../../blocks/block.service';
import { BlockContentItem } from '../../../blocks/interfaces/content.interfaces';
import { BlockViewerComponent } from '../../../blocks/component/block-viewer/block-viewer.component';
import { TagComponent } from '../../../../shared/components/tag/tag.component';
import { UsernameComponent } from '../../../../shared/components/username/username.component';

interface BlockState {
  courseBlockId: number;
  blockId: number;
  blockName: string;
  order: number;
  completed: boolean;
  loading: boolean;
  content: BlockContentItem[];
  contentLoading: boolean;
  showContent: boolean;
}

@Component({
  selector: 'studer-course-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    TranslateModule,
    BlockViewerComponent,
    TagComponent,
    UsernameComponent,
  ],
  templateUrl: './course-detail.component.html',
  styleUrls: ['./course-detail.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CourseDetailComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();

  private readonly route = inject(ActivatedRoute);
  private readonly courseService = inject(CourseService);
  private readonly blockService = inject(BlockService);

  course: CourseResponseDTO | null = null;
  loading = false;
  error = false;

  blockStates: BlockState[] = [];
  favouriteLoading = false;

  ngOnInit(): void {
    this.route.paramMap.pipe(
      takeUntil(this.destroy$),
      switchMap(params => {
        const id = Number(params.get('id'));
        this.loading = true;
        this.error = false;
        return this.courseService.getById(id);
      }),
    ).subscribe({
      next: (course) => {
        this.course = course;
        this.loading = false;
        this.initBlockStates(course);
      },
      error: () => {
        this.loading = false;
        this.error = true;
      },
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  get completionPercentage(): number {
    if (!this.blockStates.length) return 0;
    const completed = this.blockStates.filter(b => b.completed).length;
    return Math.round((completed / this.blockStates.length) * 100);
  }

  get completedCount(): number {
    return this.blockStates.filter(b => b.completed).length;
  }

  getAverageRating(): number {
    if (!this.course || this.course.ratingCount === 0) return 0;
    return Math.round((this.course.ratingSum / this.course.ratingCount) * 10) / 10;
  }

  toggleFavourite(): void {
    if (!this.course || this.favouriteLoading) return;
    this.favouriteLoading = true;
    const newState = !this.course.favourite;
    const action = newState
      ? this.courseService.addFavourite(this.course.id)
      : this.courseService.removeFavourite(this.course.id);

    action.subscribe({
      next: () => {
        if (this.course) {
          this.course.favourite = newState;
          this.course.favouriteCount += newState ? 1 : -1;
        }
        this.favouriteLoading = false;
      },
      error: () => {
        this.favouriteLoading = false;
      },
    });
  }

  toggleBlockComplete(index: number): void {
    const state = this.blockStates[index];
    if (!state || state.loading) return;

    const newCompleted = !state.completed;
    state.loading = true;

    const interaction: UserCourseBlockRequestDTO = {
      courseBlockId: state.courseBlockId,
      completed: newCompleted,
      duration: 0,
      attempts: 0,
    };

    this.courseService.saveBlockInteraction(interaction).subscribe({
      next: () => {
        state.completed = newCompleted;
        state.loading = false;
      },
      error: () => {
        state.loading = false;
      },
    });
  }

  toggleBlockContent(index: number): void {
    const state = this.blockStates[index];
    if (!state) return;

    if (state.showContent) {
      state.showContent = false;
      return;
    }

    state.showContent = true;

    if (state.content.length > 0 || state.contentLoading) return;

    state.contentLoading = true;
    this.blockService.getBlockByVersion(state.blockId).subscribe({
      next: (block) => {
        try {
          state.content = JSON.parse(block.version.content) as BlockContentItem[];
        } catch {
          state.content = [];
        }
        state.contentLoading = false;
      },
      error: () => {
        state.contentLoading = false;
      },
    });
  }

  private initBlockStates(course: CourseResponseDTO): void {
    this.blockStates = course.blocks.map(b => ({
      courseBlockId: b.id,
      blockId: b.version.id,
      blockName: b.blockName,
      order: b.order,
      completed: false,
      loading: false,
      content: [],
      contentLoading: false,
      showContent: false,
    }));
  }

  trackByCourseBlockId(index: number, state: BlockState): number {
    return state.courseBlockId;
  }
}
