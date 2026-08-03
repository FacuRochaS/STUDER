import { Component, OnInit, inject, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { Subject, takeUntil, delay } from 'rxjs';
import { DiscussionService } from '../discussion.service';
import { DiscussionCreateRequestDTO, DiscussionResponseDTO } from '../discussion.model';
import { DiscussionTabsComponent, ExploreFilters } from './discussion-tabs/discussion-tabs.component';
import { DiscussionListComponent } from './discussion-list/discussion-list.component';
import { LoaderComponent } from '../../../shared/components/loader/loader.component';
import { DiscussionCreateFormComponent } from './discussion-create-form/discussion-create-form.component';
import { AutoAnimateDirective } from '../../../shared/directives/auto-animate.directive';

@Component({
  selector: 'app-discussion',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TranslateModule,
    DiscussionTabsComponent,
    DiscussionListComponent,
    LoaderComponent,
    DiscussionCreateFormComponent,
    AutoAnimateDirective,
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
  selectedCategory = 'yours';

  currentExploreFilters: ExploreFilters = {
    tags: [],
    lastDays: null,
    activityHours: 24
  };

  ngOnInit(): void {
    this.loadDiscussionsByCategory('yours');
  }

  onCategorySelected(category: string): void {
    this.selectedCategory = category;
    this.showCreateForm = false;
    this.loadDiscussionsByCategory(category);
  }

  toggleCreate(): void {
    this.showCreateForm = !this.showCreateForm;
    if (this.showCreateForm && this.selectedCategory === 'explore') {
      this.selectedCategory = 'yours';
    }
  }

  onFiltersChanged(filters: ExploreFilters): void {
    this.currentExploreFilters = filters;
    this.loadDiscussionsByCategory('explore');
  }

  loadDiscussionsByCategory(category: string): void {
    this.loading = true;
    this.discussions = [];
    const observable = this.getObservableForCategory(category).pipe(delay(300));
    observable.subscribe({
      next: (response) => {
        this.discussions = response.discussions;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  private getObservableForCategory(category: string) {
    switch (category) {
      case 'yours': return this.discussionService.getMyOwnDiscussions();
      case 'favourites': return this.discussionService.getMyFavouriteDiscussions();
      case 'recent': return this.discussionService.getMyDiscussions();
      case 'popular': return this.discussionService.getPopularDiscussions();
      case 'new': return this.discussionService.getNewDiscussions();
      case 'explore': return this.discussionService.getPublicDiscussions(0,
        this.currentExploreFilters.tags,
        this.currentExploreFilters.lastDays ?? undefined,
        this.currentExploreFilters.activityHours);
      default: return this.discussionService.getMyDiscussions();
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
