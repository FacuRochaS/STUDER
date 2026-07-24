import { Component, Input, OnChanges, SimpleChanges, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, takeUntil } from 'rxjs';
import { BlockResponseDTO, BlockCompleteResponseDTO, BlockVersionCreateRequestDTO, BlockForkCreateRequestDTO, BlockVersionResponseDTO } from '../../block.model';
import { BlockHeaderComponent } from '../block-header/block-header.component';
import { TabsComponent, Tab } from '../../../../shared/components/tabs/tabs.component';
import { BlockViewerComponent } from '../block-viewer/block-viewer.component';
import { InfoTabComponent } from '../tabs/info-tab/info-tab.component';
import { StatsTabComponent } from '../tabs/stats-tab/stats-tab.component';
import { BlockContentItem } from '../../interfaces/content.interfaces';
import { TranslateModule } from '@ngx-translate/core';
import { ModalService } from '../../../../shared/services/modal.service';
import { BlockEditorComponent } from '../../editor/block-editor.component';
import { BlockService } from '../../block.service';
import { AuthStateService } from '../../../../core/auth/auth-state.service';
import { User } from '../../../users/user.model';

@Component({
  selector: 'studer-block-card',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TranslateModule,
    BlockHeaderComponent,
    TabsComponent,
    BlockViewerComponent,
    InfoTabComponent,
    StatsTabComponent,
  ],
  templateUrl: './block-card.component.html',
  styleUrls: ['./block-card.component.css']
})
export class BlockCardComponent implements OnInit, OnDestroy, OnChanges {
  private readonly destroy$ = new Subject<void>();
  private modalService = inject(ModalService);
  private blockService = inject(BlockService);
  private authState = inject(AuthStateService);
  @Input() block!: BlockResponseDTO | BlockCompleteResponseDTO;
  @Input() startCollapsed = false;

  currentUser: User | null = null;
  parsedContent: BlockContentItem[] = [];
  contentVisible = true;

  /** Flattened view for block-header (all types share slug/difficulty/isFork/likes). */
  get headerBlock(): BlockResponseDTO | BlockCompleteResponseDTO { return this.block; }

  /** The parsed content string, regardless of DTO variant. */
  private getVersionContent(b: BlockResponseDTO | BlockCompleteResponseDTO): string | undefined {
    if (!b) return undefined;
    if ('version' in b && b.version) {
      return b.version.content;
    }
    if ('versions' in b && (b as BlockCompleteResponseDTO).versions?.length) {
      const versions = (b as BlockCompleteResponseDTO).versions;
      return versions[versions.length - 1].content;
    }
    return undefined;
  }

  /** Version selector - only used when the DTO has multiple versions. */
  get hasMultipleVersions(): boolean {
    return 'versions' in this.block && (this.block as BlockCompleteResponseDTO).versions?.length > 1;
  }

  get allVersions(): BlockVersionResponseDTO[] {
    if ('versions' in this.block) return (this.block as BlockCompleteResponseDTO).versions ?? [];
    if ('version' in this.block && this.block.version) return [this.block.version];
    return [];
  }

  selectedVersionId: number | null = null;

  tabs: Tab[] = [
    { id: 'content', label: 'blocks.detail.tabs.content' },
    { id: 'info', label: 'blocks.detail.tabs.info' },
    { id: 'stats', label: 'blocks.tree.tab_stats' },
  ];
  activeTabId: string = 'content';

  ngOnInit(): void {
    this.authState.user$.pipe(takeUntil(this.destroy$)).subscribe(u => this.currentUser = u);
    if (this.startCollapsed) { this.contentVisible = false; }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['block']) {
      const content = this.getVersionContent(this.block);
      this.parsedContent = this.parseContent(content);
      if (this.allVersions.length > 1) {
        this.selectedVersionId = this.allVersions[this.allVersions.length - 1].id;
      }
    }
  }

  onVersionChange(): void {
    if (!this.selectedVersionId) return;
    const v = this.allVersions.find(x => x.id === this.selectedVersionId);
    if (v) {
      this.parsedContent = this.parseContent(v.content);
    }
  }

  onTabChange(tabId: string): void { this.activeTabId = tabId; }
  toggleContent(): void { this.contentVisible = !this.contentVisible; }

  onOpenTree(): void {
    if (!this.block) return;
    import('../block-tree/block-tree.component').then(m => {
      this.modalService.open(m.BlockTreeComponent, {
        title: 'blocks.tree.title',
        inputs: { blockId: this.block.id },
      });
    });
  }

  onEdit(): void {
    if (!this.block) return;
    const isOwner = 'owner' in this.block && this.currentUser?.id === this.block.owner.id;
    this.modalService.open(BlockEditorComponent, {
      title: isOwner ? 'blocks.editor.actions.edit' : 'blocks.editor.actions.fork',
      inputs: {
        mode: 'edit',
        blockId: this.block.id,
        blockName: this.block.name,
        blockDifficulty: this.block.difficulty,
        blockTags: this.block.tags,
        initialContent: this.parsedContent,
      },
      outputs: {
        save: (data: any) => {
          const obs = isOwner
            ? this.blockService.versionBlock(data as BlockVersionCreateRequestDTO)
            : this.blockService.forkBlock({ ...data, name: this.block.name, difficulty: (this.block as BlockResponseDTO).difficulty, tags: this.block.tags, blockId: this.block.id, slug: '' } as BlockForkCreateRequestDTO);
          obs.subscribe({ next: () => this.modalService.close(), error: () => {} });
        },
      },
    });
  }

  private parseContent(content: string | undefined): BlockContentItem[] {
    if (!content) return [];
    try { return JSON.parse(content); } catch { return []; }
  }
}
