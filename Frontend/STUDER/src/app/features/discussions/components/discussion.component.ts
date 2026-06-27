import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { DiscussionService } from '../discussion.service';
import { DiscussionCreateRequestDTO, DiscussionResponseDTO } from '../discussion.model';
import { DiscussionSidebarComponent, DiscussionCategory } from './discussion-sidebar/discussion-sidebar.component';
import { DiscussionListComponent } from './discussion-list/discussion-list.component';
import { ExploreFiltersComponent, ExploreFilters } from './explore-filters/explore-filters.component';
import { LoaderComponent } from '../../../shared/components/loader/loader.component';
import { ModalService } from '../../../shared/services/modal.service';
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
  ],
  templateUrl: './discussion.component.html',
  styleUrls: ['./discussion.component.css']
})
export class DiscussionComponent implements OnInit {
  private discussionService = inject(DiscussionService);
  private modalService = inject(ModalService);

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

  onNewDiscussion(): void {
    this.modalService.open(DiscussionCreateFormComponent, {
      title: 'discussions.create_title',
      outputs: {
        save: (request: DiscussionCreateRequestDTO) => {
          this.discussionService.create(request).subscribe({
            next: () => {
              this.modalService.close();
              this.onCategorySelected('yours');
            },
            error: () => {
              // Handle error
            }
          });
        },
        cancel: () => {
          this.modalService.close();
        }
      }
    });
  }
}
