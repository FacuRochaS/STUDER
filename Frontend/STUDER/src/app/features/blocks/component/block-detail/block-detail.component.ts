import { Component, OnInit, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { BlockService } from '../../block.service';
import { BlockCompleteResponseDTO } from '../../block.model';
import { BlockHeaderComponent } from '../block-header/block-header.component';
import { TabsComponent, Tab } from '../../../../shared/components/tabs/tabs.component';

import { InfoTabComponent } from '../tabs/info-tab/info-tab.component';
import { VersionsTabComponent } from '../tabs/versions-tab/versions-tab.component';
import { StatsTabComponent } from '../tabs/stats-tab/stats-tab.component';
import { BlockContentItem } from '../../interfaces/content.interfaces';
import { TranslateModule } from '@ngx-translate/core';
import {BlockViewerComponent} from '../block-viewer/block-viewer.component';

@Component({
  selector: 'studer-block-detail',
  standalone: true,
  imports: [
    CommonModule,
    TranslateModule,
    BlockHeaderComponent,
    TabsComponent,
    BlockViewerComponent,
    InfoTabComponent,
    VersionsTabComponent,
    StatsTabComponent,
  ],
  templateUrl: './block-detail.component.html',
  styleUrls: ['./block-detail.component.css']
})
export class BlockDetailComponent implements OnInit {
  @Input() block: BlockCompleteResponseDTO | null = null;

  loading = true;
  error = false;

  tabs: Tab[] = [
    { id: 'content', label: 'blocks.detail.tabs.content' },
    { id: 'info', label: 'blocks.detail.tabs.info' },
    { id: 'versions', label: 'blocks.detail.tabs.versions' },
    { id: 'stats', label: 'blocks.detail.tabs.stats' },
  ];
  activeTabId: string = 'content';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly blockService: BlockService
  ) {}

  ngOnInit(): void {
    if (this.block) {
      // If block is provided via input, don't fetch from route
      this.loading = false;
    } else {
      this.route.paramMap.subscribe(params => {
        const id = params.get('id');
        if (id) {
          this.loadBlock(Number(id));
        } else {
          this.loading = false;
          this.error = true;
        }
      });
    }
  }

  loadBlock(id: number): void {
    this.loading = true;
    this.error = false;
    this.blockService.getBlockVersion(id).subscribe({
      next: (block) => {
        this.block = block;
        this.loading = false;
      },
      error: () => {
        this.error = true;
        this.loading = false;
      }
    });
  }

  onTabChange(tabId: string): void {
    this.activeTabId = tabId;
  }

  parseContent(content: string | undefined): BlockContentItem[] {
    if (!content) return [];
    try {
      return JSON.parse(content);
    } catch (e) {
      console.error("Failed to parse block content", e);
      return [];
    }
  }
}
