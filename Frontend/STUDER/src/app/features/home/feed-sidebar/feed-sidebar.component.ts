import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'studer-feed-sidebar',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './feed-sidebar.component.html',
  styleUrls: ['./feed-sidebar.component.css']
})
export class FeedSidebarComponent {
  @Output() categorySelected = new EventEmitter<string>();

  selectedCategory = 'recent';

  selectCategory(category: string): void {
    this.selectedCategory = category;
    this.categorySelected.emit(category);
  }
}
