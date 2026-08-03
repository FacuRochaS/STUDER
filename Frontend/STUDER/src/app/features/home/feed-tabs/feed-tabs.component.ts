import { Component, EventEmitter, Output, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { AutoAnimateDirective } from '../../../shared/directives/auto-animate.directive';

@Component({
  selector: 'studer-feed-tabs',
  standalone: true,
  imports: [CommonModule, TranslateModule, AutoAnimateDirective],
  templateUrl: './feed-tabs.component.html',
  styleUrls: ['./feed-tabs.component.css']
})
export class FeedTabsComponent {
  @Input() activeTab = 'recent';
  @Input() showCreate = false;
  @Output() tabSelected = new EventEmitter<string>();
  @Output() createToggled = new EventEmitter<void>();

  tabs = [
    { id: 'yours', label: 'feed.yours', icon: 'pi pi-user' },
    { id: 'following', label: 'feed.following', icon: 'pi pi-users' },
    { id: 'popular', label: 'feed.popular', icon: 'pi pi-bolt' },
    { id: 'new', label: 'feed.new_posts', icon: 'pi pi-sparkles' },
  ];

  selectTab(id: string): void { this.tabSelected.emit(id); }
  toggleCreate(): void { this.createToggled.emit(); }
}
