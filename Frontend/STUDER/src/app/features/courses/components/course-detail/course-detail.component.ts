import { Component, Input, Output, EventEmitter, OnDestroy, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subject, takeUntil } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';
import { CourseService } from '../../course.service';
import { CourseResponseDTO, UserCourseBlockRequestDTO } from '../../course.model';
import { BlockService } from '../../../blocks/block.service';
import { BlockResponseDTO } from '../../../blocks/block.model';
import { BlockContentItem, ActivityContentData } from '../../../blocks/interfaces/content.interfaces';
import { BlockHeaderComponent } from '../../../blocks/component/block-header/block-header.component';
import { BlockViewerComponent } from '../../../blocks/component/block-viewer/block-viewer.component';
import { ActivityViewerComponent } from '../../../blocks/activity/viewer/activity-viewer.component';
import { InfoTabComponent } from '../../../blocks/component/tabs/info-tab/info-tab.component';
import { TagComponent } from '../../../../shared/components/tag/tag.component';
import { UsernameComponent } from '../../../../shared/components/username/username.component';
import { TabsComponent, Tab } from '../../../../shared/components/tabs/tabs.component';
import { StatsTabComponent } from '../../../blocks/component/tabs/stats-tab/stats-tab.component';
import { ModalService } from '../../../../shared/services/modal.service';
import { AuthStateService } from '../../../../core/auth/auth-state.service';
import { User } from '../../../users/user.model';
import { CourseCreateComponent } from '../course-create/course-create.component';
import { LoaderComponent } from '../../../../shared/components/loader/loader.component';

interface BlockState {
  courseBlockId: number;
  blockId: number;
  blockName: string;
  order: number;
  completed: boolean;
  loading: boolean;
  content: BlockContentItem[];
  showContent: boolean;
  hasActivity: boolean;
  fullBlock: BlockResponseDTO | null;
  activeTab: string;
}

@Component({
  selector: 'studer-course-detail',
  standalone: true,
  imports: [
    CommonModule,
    TranslateModule,
    BlockHeaderComponent,
    BlockViewerComponent,
    ActivityViewerComponent,
    InfoTabComponent,
    TagComponent,
    UsernameComponent,
    TabsComponent,
    StatsTabComponent,
    LoaderComponent,
  ],
  templateUrl: './course-detail.component.html',
  styleUrls: ['./course-detail.component.css'],

})
export class CourseDetailComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();

  /** When embedded inside course-explore, pass the course via input */
  @Input() courseId: number | null = null;
  /** When used as child of explore, emit back to go to list */
  @Input() backToList = false;
  @Output() closed = new EventEmitter<void>();

  private readonly courseService = inject(CourseService);
  private readonly blockService = inject(BlockService);
  private readonly modalService = inject(ModalService);
  private readonly authState = inject(AuthStateService);

  currentUser: User | null = null;
  course: CourseResponseDTO | null = null;
  loading = false;
  error = false;

  blockStates: BlockState[] = [];
  favouriteLoading = false;

  activeTabId = 'curso';

  tabs: Tab[] = [
    { id: 'curso', label: 'course.detail.tab_curso' },
    { id: 'estadisticas', label: 'course.detail.tab_estadisticas' },
  ];

  blockTabs: Tab[] = [
    { id: 'content', label: 'blocks.detail.tabs.content' },
    { id: 'info', label: 'blocks.detail.tabs.info' },
    { id: 'stats', label: 'blocks.tree.tab_stats' },
  ];

  ngOnInit(): void {
    this.authState.user$.pipe(takeUntil(this.destroy$)).subscribe(u => this.currentUser = u);
    if (this.courseId) {
      this.loadCourse(this.courseId);
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  get isOwner(): boolean {
    return !!this.currentUser && !!this.course && this.currentUser.id === this.course.owner.id;
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

  getAverageUserRating(): string {
    if (!this.course || this.course.ratingCount === 0) return '0.0';
    return (this.course.ratingSum / this.course.ratingCount).toFixed(1);
  }

  onTabChange(tabId: string): void {
    this.activeTabId = tabId;
  }

  onBlockTabChange(index: number, tabId: string): void {
    if (this.blockStates[index]) {
      this.blockStates[index].activeTab = tabId;
    }
  }

  onEdit(): void {
    if (!this.course) return;
    this.modalService.open(CourseCreateComponent, {
      title: 'course.create.edit_title',
      inputs: {
        mode: 'edit',
        editCourse: this.course,
      },
      outputs: {
        saved: () => {
          this.modalService.close();
          this.loadCourse(this.course!.id);
        },
      },
    });
  }

  private loadCourse(id: number): void {
    this.loading = true;
    this.courseService.getById(id).subscribe({
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

  onBlockCompleted(index: number): void {
    const state = this.blockStates[index];
    if (!state || state.loading || state.completed) return;

    state.loading = true;
    this.saveCompletion(state, true);
  }

  toggleBlockComplete(index: number): void {
    const state = this.blockStates[index];
    if (!state || state.loading) return;

    state.loading = true;
    this.saveCompletion(state, !state.completed);
  }

  private saveCompletion(state: BlockState, completed: boolean): void {
    const interaction: UserCourseBlockRequestDTO = {
      courseBlockId: state.courseBlockId,
      completed,
      duration: 0,
      attempts: 0,
    };

    this.courseService.saveBlockInteraction(interaction).subscribe({
      next: () => {
        state.completed = completed;
        state.loading = false;
        if (completed) {
          state.showContent = false;
        }
      },
      error: () => {
        state.loading = false;
      },
    });
  }

  toggleBlockContent(index: number): void {
    const state = this.blockStates[index];
    if (!state) return;
    state.showContent = !state.showContent;
  }

  onEditBlock(index: number): void {
    const state = this.blockStates[index];
    if (!state || !state.fullBlock) return;
    const isOwner = this.currentUser?.id === this.course?.owner.id;
    const title = isOwner ? 'blocks.editor.actions.edit' : 'blocks.editor.actions.fork';

  }

  onOpenTree(index: number): void {
    const state = this.blockStates[index];
    if (!state) return;
    import('../../../blocks/component/block-tree/block-tree.component').then(m => {
      this.modalService.open(m.BlockTreeComponent, {
        title: 'blocks.tree.title',
        inputs: { blockId: state.blockId },
      });
    });
  }

  hasActivityContent(index: number): boolean {
    return this.blockStates[index]?.hasActivity ?? false;
  }

  private initBlockStates(course: CourseResponseDTO): void {
    this.blockStates = course.blocks.map(b => {
      const content = this.parseContent(b.version.content);
      const hasActivity = content.some(c => c.type === 'activity');
      const completed = b.completed ?? false;
      const initialBlock: BlockResponseDTO = {
        id: b.blockId,
        createdDatetime: course.createdDatetime,
        lastUpdatedDatetime: course.lastUpdatedDatetime,
        slug: b.blockName.toLowerCase().replace(/\s+/g, '-'),
        name: b.blockName,
        difficulty: 'NORMAL',
        isFork: false,
        likedByCurrentUser: false,
        likeCount: 0,
        tags: [],
        version: null,
        owner: course.owner,
      } as unknown as BlockResponseDTO;
      return {
        courseBlockId: b.id,
        blockId: b.blockId,
        blockName: b.blockName,
        order: b.order,
        completed,
        loading: false,
        content,
        showContent: !completed,
        hasActivity,
        fullBlock: initialBlock,
        activeTab: 'content',
      };
    });
    course.blocks.forEach((b, i) => {
      this.blockService.getBlockByVersion(b.version.id).pipe(takeUntil(this.destroy$)).subscribe({
        next: (block) => {
          if (this.blockStates[i]) this.blockStates[i].fullBlock = block;
        },
        error: (err) => {
          console.error('Failed to load block details', err);
        },
      });
    });
  }

  private parseContent(raw: string | undefined): BlockContentItem[] {
    if (!raw) return [];
    try {
      return JSON.parse(raw) as BlockContentItem[];
    } catch {
      return [];
    }
  }

  asActivityData(data: unknown): ActivityContentData {
    return data as ActivityContentData;
  }

}
