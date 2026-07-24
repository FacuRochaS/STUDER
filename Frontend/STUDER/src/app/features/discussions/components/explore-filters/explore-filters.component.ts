import { Component, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import {TagInputComponent} from '../../../../shared/components/tag-input/tag-input.component';


export interface ExploreFilters {
  tags: string[];
  lastDays: number | null;
  activityHours: number;
}

@Component({
  selector: 'studer-explore-filters',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule, TagInputComponent],
  templateUrl: './explore-filters.component.html',
  styleUrls: ['./explore-filters.component.css']
})
export class ExploreFiltersComponent {
  @Output() filtersChanged = new EventEmitter<ExploreFilters>();

  filters: ExploreFilters = {
    tags: [],
    lastDays: null,
    activityHours: 24
  };

  applyFilters(): void {
    this.filtersChanged.emit(this.filters);
  }

  resetFilters(): void {
    this.filters = {
      tags: [],
      lastDays: null,
      activityHours: 24
    };
    this.applyFilters();
  }
}
