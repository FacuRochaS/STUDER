import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';

export type DiscussionCategory = 'yours' | 'favourites' | 'recent' | 'popular' | 'new' | 'explore' | 'create';

@Component({
  selector: 'app-discussion-sidebar',
  templateUrl: './discussion-sidebar.component.html',
  styleUrls: ['./discussion-sidebar.component.css'],
  standalone: true,
  imports: [CommonModule, TranslateModule]
})
export class DiscussionSidebarComponent {
  @Output() categorySelected = new EventEmitter<DiscussionCategory>();

  selectedCategory: DiscussionCategory = 'recent';

  selectCategory(category: DiscussionCategory) {
    this.selectedCategory = category;
    this.categorySelected.emit(category);
  }
}
