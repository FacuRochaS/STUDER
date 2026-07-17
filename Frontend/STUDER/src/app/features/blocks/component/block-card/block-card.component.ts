import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BlockResponseDTO } from '../../block.model';
import { BlockHeaderComponent } from '../block-header/block-header.component';
import { TabsComponent, Tab } from '../../../../shared/components/tabs/tabs.component';
import { BlockViewerComponent } from '../block-viewer/block-viewer.component';
import { InfoTabComponent } from '../tabs/info-tab/info-tab.component';
import { BlockContentItem } from '../../interfaces/content.interfaces';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'studer-block-card',
  standalone: true,
  imports: [
    CommonModule,
    TranslateModule,
    BlockHeaderComponent,
    TabsComponent,
    BlockViewerComponent,
    InfoTabComponent,
  ],
  templateUrl: './block-card.component.html',
  styleUrls: ['./block-card.component.css']
})
export class BlockCardComponent implements OnChanges {
  @Input() block!: BlockResponseDTO;

  parsedContent: BlockContentItem[] = [];

  tabs: Tab[] = [
    { id: 'content', label: 'blocks.detail.tabs.content' },
    { id: 'info', label: 'blocks.detail.tabs.info' },
  ];
  activeTabId: string = 'content';

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['block']) {
      this.parsedContent = this.parseContent(this.block?.version?.content);
    }
  }

  onTabChange(tabId: string): void {
    this.activeTabId = tabId;
  }

  private parseContent(content: string | undefined): BlockContentItem[] {
    if (!content) return [];
    try {
      return JSON.parse(content);
    } catch (e) {
      console.error("Failed to parse block content", e);
      return [];
    }
  }
}
