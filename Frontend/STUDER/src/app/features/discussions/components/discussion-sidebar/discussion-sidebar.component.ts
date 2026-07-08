import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';

// Usaremos un tipo para asegurarnos de que solo se emitan valores válidos
export type DiscussionCategory = 'yours' | 'favourites' | 'recent' | 'popular' | 'new' | 'explore';

@Component({
  selector: 'app-discussion-sidebar',
  templateUrl: './discussion-sidebar.component.html',
  styleUrls: ['./discussion-sidebar.component.css'],
  standalone: true,
  imports: [CommonModule, TranslateModule]
})
export class DiscussionSidebarComponent {
  @Output() categorySelected = new EventEmitter<DiscussionCategory>();
  @Output() newDiscussion = new EventEmitter<void>();

  selectedCategory: DiscussionCategory = 'recent'; // Valor inicial por defecto

  selectCategory(category: DiscussionCategory) {
    this.selectedCategory = category;
    this.categorySelected.emit(category);
  }

  openNew() {
    this.newDiscussion.emit();
  }
}
