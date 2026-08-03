import { Component, EventEmitter, Output, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { TagInputComponent } from '../../../../shared/components/tag-input/tag-input.component';
import { AutoAnimateDirective } from '../../../../shared/directives/auto-animate.directive';

export interface ExploreFilters {
  tags: string[];
  lastDays: number | null;
  activityHours: number;
}

@Component({
  selector: 'studer-discussion-tabs',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule, TagInputComponent, AutoAnimateDirective],
  templateUrl: './discussion-tabs.component.html',
  styleUrls: ['./discussion-tabs.component.css']
})
export class DiscussionTabsComponent {
  @Input() activeTab = 'recent';
  @Input() showCreate = false;
  @Output() tabSelected = new EventEmitter<string>();
  @Output() createToggled = new EventEmitter<void>();
  @Output() filtersChanged = new EventEmitter<ExploreFilters>();

  tabs = [
    { id: 'yours', label: 'discussion.sidebar.yours', icon: 'pi pi-user' },
    { id: 'favourites', label: 'discussion.sidebar.favourites', icon: 'pi pi-star' },
    { id: 'recent', label: 'discussion.sidebar.recent', icon: 'pi pi-history' },
    { id: 'popular', label: 'discussion.sidebar.popular', icon: 'pi pi-bolt' },
    { id: 'new', label: 'discussion.sidebar.new_discussions', icon: 'pi pi-sparkles' },
    { id: 'explore', label: 'discussion.sidebar.explore', icon: 'pi pi-compass' },
  ];

  filters: ExploreFilters = { tags: [], lastDays: null, activityHours: 24 };

  selectTab(id: string): void { this.tabSelected.emit(id); }
  toggleCreate(): void { this.createToggled.emit(); }

  applyFilters(): void {
    this.filtersChanged.emit({ ...this.filters });
  }

  resetFilters(): void {
    this.filters = { tags: [], lastDays: null, activityHours: 24 };
    this.applyFilters();
  }
}
