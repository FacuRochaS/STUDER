import { Component, Input, OnChanges, SimpleChanges, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subject, takeUntil } from 'rxjs';
import { BlockResponseDTO, BlockVersionCreateRequestDTO, BlockForkCreateRequestDTO } from '../../block.model';
import { BlockHeaderComponent } from '../block-header/block-header.component';
import { TabsComponent, Tab } from '../../../../shared/components/tabs/tabs.component';
import { BlockViewerComponent } from '../block-viewer/block-viewer.component';
import { InfoTabComponent } from '../tabs/info-tab/info-tab.component';
import { BlockContentItem } from '../../interfaces/content.interfaces';
import { TranslateModule } from '@ngx-translate/core';
import { ModalService } from '../../../../shared/services/modal.service';
import { BlockTreeComponent } from '../block-tree/block-tree.component';
import { BlockEditorComponent } from '../../editor/block-editor.component';
import { BlockService } from '../../block.service';
import { AuthStateService } from '../../../../core/auth/auth-state.service';
import { User } from '../../../../features/users/user.model';

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
export class BlockCardComponent implements OnInit, OnDestroy, OnChanges {
  private readonly destroy$ = new Subject<void>();
  private modalService = inject(ModalService);
  private blockService = inject(BlockService);
  private authState = inject(AuthStateService);
  @Input() block!: BlockResponseDTO;
  @Input() startCollapsed = false;

  currentUser: User | null = null;
  parsedContent: BlockContentItem[] = [];
  contentVisible = true;

  tabs: Tab[] = [
    { id: 'content', label: 'blocks.detail.tabs.content' },
    { id: 'info', label: 'blocks.detail.tabs.info' },
  ];
  activeTabId: string = 'content';

  ngOnInit(): void {
    this.authState.user$.pipe(takeUntil(this.destroy$)).subscribe(u => this.currentUser = u);
    if (this.startCollapsed) {
      this.contentVisible = false;
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['block']) {
      this.parsedContent = this.parseContent(this.block?.version?.content);
    }
  }

  onTabChange(tabId: string): void {
    this.activeTabId = tabId;
  }

  toggleContent(): void {
    this.contentVisible = !this.contentVisible;
  }

  onOpenTree(): void {
    if (!this.block) return;
    this.modalService.open(BlockTreeComponent, {
      title: 'blocks.tree.title',
      inputs: { blockId: this.block.id },
    });
  }

  onEdit(): void {
    if (!this.block) return;
    const isOwner = this.currentUser?.id === this.block.owner.id;
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
            : this.blockService.forkBlock({ ...data, name: this.block.name, difficulty: this.block.difficulty, tags: this.block.tags, blockId: this.block.id, slug: '' } as BlockForkCreateRequestDTO);
          obs.subscribe({
            next: () => this.modalService.close(),
            error: () => {},
          });
        },
      },
    });
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
