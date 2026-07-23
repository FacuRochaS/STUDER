import { Component, OnInit, inject, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { Subject, takeUntil } from 'rxjs';
import { DiscussionService } from '../discussion.service';
import { DiscussionCreateRequestDTO, DiscussionResponseDTO } from '../discussion.model';
import { DiscussionSidebarComponent, DiscussionCategory } from './discussion-sidebar/discussion-sidebar.component';
import { DiscussionListComponent } from './discussion-list/discussion-list.component';
import { ExploreFiltersComponent, ExploreFilters } from './explore-filters/explore-filters.component';
import { LoaderComponent } from '../../../shared/components/loader/loader.component';
import { DiscussionCreateFormComponent } from './discussion-create-form/discussion-create-form.component';

@Component({
  selector: 'app-discussion',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TranslateModule,
    DiscussionSidebarComponent,
    DiscussionListComponent,
    ExploreFiltersComponent,
    LoaderComponent,
    DiscussionCreateFormComponent,
  ],
  templateUrl: './discussion.component.html',
  styleUrls: ['./discussion.component.css']
})
export class DiscussionComponent implements OnInit, OnDestroy {
  private discussionService = inject(DiscussionService);
  private destroy$ = new Subject<void>();

  showCreateForm = false;
  discussions: DiscussionResponseDTO[] = [];
  loading = false;
  selectedCategory: DiscussionCategory = 'recent';

  currentExploreFilters: ExploreFilters = {
    tags: [],
    lastDays: null,
    activityHours: 24
  };

  ngOnInit(): void {
    this.loadDiscussionsByCategory('recent');
  }

  onCategorySelected(category: DiscussionCategory): void {
    this.selectedCategory = category;
    if (category === 'create') {
      this.showCreateForm = !this.showCreateForm;
      return;
    }
    this.showCreateForm = false;
    this.loadDiscussionsByCategory(category);
  }

  onFiltersChanged(filters: ExploreFilters): void {
    this.currentExploreFilters = filters;
    if (this.selectedCategory === 'explore') {
      this.loadDiscussionsByCategory('explore');
    }
  }

  loadDiscussionsByCategory(category: DiscussionCategory): void {
    this.loading = true;
    this.discussions = [];

    const discussionObservable = this.getObservableForCategory(category);

    discussionObservable.subscribe({
      next: (response) => {
        this.discussions = response.discussions;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  private getObservableForCategory(category: DiscussionCategory) {
    switch (category) {
      case 'yours':
        return this.discussionService.getMyOwnDiscussions();
      case 'favourites':
        return this.discussionService.getMyFavouriteDiscussions();
      case 'recent':
        return this.discussionService.getMyDiscussions();
      case 'popular':
        return this.discussionService.getPopularDiscussions();
      case 'new':
        return this.discussionService.getNewDiscussions();
      case 'explore':
        return this.discussionService.getPublicDiscussions(
          0,
          this.currentExploreFilters.tags,
          this.currentExploreFilters.lastDays ?? undefined,
          this.currentExploreFilters.activityHours
        );
      default:
        return this.discussionService.getMyDiscussions();
    }
  }

  onCreateDiscussion(request: DiscussionCreateRequestDTO): void {
    this.discussionService.create(request).pipe(takeUntil(this.destroy$)).subscribe({
      next: () => {
        this.showCreateForm = false;
        this.loadDiscussionsByCategory(this.selectedCategory);
      }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
