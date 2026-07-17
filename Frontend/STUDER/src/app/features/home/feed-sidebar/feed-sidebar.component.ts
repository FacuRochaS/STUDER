import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'studer-feed-sidebar',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  template: `
    <aside class="feed-sidebar">
      <ul class="feed-sidebar__list">
        <li
          class="feed-sidebar__item"
          [class.feed-sidebar__item--active]="selectedCategory === 'recent'"
          (click)="selectCategory('recent')"
        >
          <i class="pi pi-clock feed-sidebar__icon"></i>
          <span>{{ 'feed.recent' | translate }}</span>
        </li>
        <li
          class="feed-sidebar__item"
          [class.feed-sidebar__item--active]="selectedCategory === 'popular'"
          (click)="selectCategory('popular')"
        >
          <i class="pi pi-fire feed-sidebar__icon"></i>
          <span>{{ 'feed.popular' | translate }}</span>
        </li>
        <li
          class="feed-sidebar__item"
          [class.feed-sidebar__item--active]="selectedCategory === 'following'"
          (click)="selectCategory('following')"
        >
          <i class="pi pi-users feed-sidebar__icon"></i>
          <span>{{ 'feed.following' | translate }}</span>
        </li>
      </ul>

      <hr class="feed-sidebar__divider">

      <ul class="feed-sidebar__list">
        <li
          class="feed-sidebar__item"
          [class.feed-sidebar__item--active]="selectedCategory === 'create'"
          (click)="selectCategory('create')"
        >
          <i class="pi pi-pencil feed-sidebar__icon"></i>
          <span>{{ 'feed.create_post' | translate }}</span>
        </li>
      </ul>
    </aside>
  `,
  styles: [`
    :host {
      display: block;
      background-color: var(--sidebar-bg);
      padding: 1rem;
      border-right: 1px solid var(--border);
      height: 100%;
    }

    .feed-sidebar {
      display: flex;
      flex-direction: column;
      gap: 1rem;
      height: 100%;
    }

    .feed-sidebar__list {
      list-style: none;
      padding: 0;
      margin: 0;
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
    }

    .feed-sidebar__item {
      padding: 0.75rem 1rem;
      cursor: pointer;
      border-radius: 6px;
      display: flex;
      align-items: center;
      gap: 1rem;
      color: var(--color-text-secu);
      transition: background-color 0.2s, color 0.2s;
    }

    .feed-sidebar__item:hover {
      background-color: var(--sidebar-hover-bg);
      color: var(--color-text-prim);
    }

    .feed-sidebar__item--active {
      background-color: var(--sidebar-hover-bg);
      color: var(--color-primary);
      font-weight: bold;
    }

    .feed-sidebar__icon {
      font-size: 1.2rem;
    }

    .feed-sidebar__divider {
      border: none;
      border-top: 1px solid var(--border);
      margin: 0.5rem 0;
    }
  `]
})
export class FeedSidebarComponent {
  @Output() categorySelected = new EventEmitter<string>();

  selectedCategory = 'recent';

  selectCategory(category: string): void {
    this.selectedCategory = category;
    this.categorySelected.emit(category);
  }
}
