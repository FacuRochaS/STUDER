import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Subject, takeUntil } from 'rxjs';
import { TranslatePipe } from '@ngx-translate/core';
import { DiscussionService } from '../../discussion.service';
import { DiscussionResponseDTO } from '../../discussion.model';
import { RelativeTimePipe } from '../../../../shared/pipes/relative-time.pipe';
import { TagComponent } from '../../../../shared/components/tag/tag.component';
import { UsernameComponent } from '../../../../shared/components/username/username.component';
import { LoaderComponent } from '../../../../shared/components/loader/loader.component';
import { ModalComponent } from '../../../../shared/components/modal/modal.component';
import { TagInputComponent } from '../../../../shared/components/tag-input/tag-input.component';

type TabFilter = 'PUBLIC' | 'MINE';

@Component({
  selector: 'studer-discussion-list',
  standalone: true,
  imports: [
    CommonModule, FormsModule, TranslatePipe, RelativeTimePipe,
    TagComponent, UsernameComponent, LoaderComponent,
    ModalComponent, TagInputComponent
  ],
  templateUrl: './discussion-list.component.html',
  styleUrls: ['./discussion-list.component.css']
})
export class DiscussionListComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();

  discussions: DiscussionResponseDTO[] = [];
  loading = false;
  currentPage = 0;
  hasMore = false;
  activeTab: TabFilter = 'PUBLIC';
  showCreateModal = false;

  /** Formulario para crear discusión */
  newTitle = '';
  newDescription = '';
  newTags: string[] = [];
  creating = false;
  createError: string | null = null;

  constructor(
    private readonly discussionService: DiscussionService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.loadDiscussions();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  setTab(tab: TabFilter): void {
    this.activeTab = tab;
    this.currentPage = 0;
    this.loadDiscussions();
  }

  loadDiscussions(append = false): void {
    this.loading = true;

    const source$ = this.activeTab === 'MINE'
      ? this.discussionService.getMyDiscussions(this.currentPage)
      : this.discussionService.getPublicDiscussions(this.currentPage);

    source$.pipe(takeUntil(this.destroy$)).subscribe({
      next: (page) => {
        this.discussions = append
          ? [...this.discussions, ...page.discussions]
          : page.discussions;
        this.hasMore = page.hasMore;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  loadMore(): void {
    this.currentPage++;
    this.loadDiscussions(true);
  }

  openDiscussion(discussion: DiscussionResponseDTO): void {
    this.router.navigate(['/discussions', discussion.id]);
  }

  toggleFavourite(discussion: DiscussionResponseDTO, event: MouseEvent): void {
    event.stopPropagation();

    const action$ = discussion.favourite
      ? this.discussionService.removeFavourite(discussion.id)
      : this.discussionService.addFavourite(discussion.id);

    action$.pipe(takeUntil(this.destroy$)).subscribe({
      next: () => {
        discussion.favourite = !discussion.favourite;
      }
    });
  }

  openCreateModal(): void {
    this.showCreateModal = true;
    this.createError = null;
    this.newTitle = '';
    this.newDescription = '';
    this.newTags = [];
  }

  closeCreateModal(): void {
    this.showCreateModal = false;
  }

  submitCreate(): void {
    if (!this.newTitle.trim() || !this.newDescription.trim()) return;

    this.creating = true;
    this.createError = null;

    this.discussionService
      .create({ title: this.newTitle, description: this.newDescription, tags: this.newTags })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (created) => {
          this.creating = false;
          this.showCreateModal = false;
          this.router.navigate(['/discussions', created.id]);
        },
        error: () => {
          this.creating = false;
          this.createError = 'discussions.create_error';
        }
      });
  }

  getParticipationIcon(type: string): string {
    switch (type) {
      case 'OWNER': return 'pi pi-crown';
      case 'MESSAGED': return 'pi pi-comment';
      case 'FAVOURITE': return 'pi pi-star-fill';
      default: return '';
    }
  }
}

