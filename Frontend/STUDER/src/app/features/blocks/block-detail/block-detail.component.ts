import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { BlockService } from '../block.service';
import { BlockCompleteResponseDTO } from '../block.model';
import { BlockHeaderComponent } from '../block-header/block-header.component';
import { TabsComponent, Tab } from '../../../shared/components/tabs/tabs.component';
import { BlockViewerComponent } from '../viewer/block-viewer.component';
import { InfoTabComponent } from '../tabs/info-tab/info-tab.component';
import { VersionsTabComponent } from '../tabs/versions-tab/versions-tab.component';
import { StatsTabComponent } from '../tabs/stats-tab/stats-tab.component';
import { BlockContentItem } from '../interfaces/content.interfaces';

@Component({
  selector: 'studer-block-detail',
  standalone: true,
  imports: [
    CommonModule,
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
  block: BlockCompleteResponseDTO | null = null;
  loading = true;
  error = false;

  tabs: Tab[] = [
    { id: 'content', label: 'Contenido' },
    { id: 'info', label: 'Información' },
    { id: 'versions', label: 'Versiones' },
    { id: 'stats', label: 'Estadísticas' },
  ];
  activeTabId: string = 'content';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly blockService: BlockService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.loadBlock(Number(id));
      }
    });
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
