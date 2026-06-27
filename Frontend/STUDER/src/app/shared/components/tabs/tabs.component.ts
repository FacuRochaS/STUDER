import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface Tab {
  id: string;
  label: string;
  icon?: string;
}

@Component({
  selector: 'studer-tabs',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './tabs.component.html',
  styleUrls: ['./tabs.component.css']
})
export class TabsComponent {
  @Input() tabs: Tab[] = [];
  @Input() activeTabId: string | null = null;
  @Output() tabChange = new EventEmitter<string>();

  selectTab(tabId: string): void {
    if (tabId !== this.activeTabId) {
      this.tabChange.emit(tabId);
    }
  }
}
