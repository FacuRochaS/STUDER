import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';

export type CourseCategory = 'recent' | 'popular' | 'yours' | 'favourites' | 'create'| 'explore' ;

@Component({
  selector: 'studer-course-sidebar',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './course-sidebar.component.html',
  styleUrls: ['./course-sidebar.component.css'],
})
export class CourseSidebarComponent {
  @Output() categorySelected = new EventEmitter<CourseCategory>();

  selectedCategory: CourseCategory = 'recent';

  selectCategory(category: CourseCategory): void {
    this.selectedCategory = category;
    this.categorySelected.emit(category);
  }
}
