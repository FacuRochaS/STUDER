import { Component, OnInit, Input, OnChanges, SimpleChanges, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subject, takeUntil } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { BlockService } from '../../block.service';
import { BlockCompleteResponseDTO, BlockVersionCreateRequestDTO, BlockForkCreateRequestDTO } from '../../block.model';
import { BlockHeaderComponent } from '../block-header/block-header.component';
import { TabsComponent, Tab } from '../../../../shared/components/tabs/tabs.component';
import { InfoTabComponent } from '../tabs/info-tab/info-tab.component';
import { VersionsTabComponent } from '../tabs/versions-tab/versions-tab.component';
import { StatsTabComponent } from '../tabs/stats-tab/stats-tab.component';
import { BlockTreeComponent } from '../block-tree/block-tree.component';
import { BlockEditorComponent } from '../../editor/block-editor.component';
import { BlockContentItem } from '../../interfaces/content.interfaces';
import { TranslateModule } from '@ngx-translate/core';
import { BlockViewerComponent } from '../block-viewer/block-viewer.component';
import { ModalService } from '../../../../shared/services/modal.service';
import { AuthStateService } from '../../../../core/auth/auth-state.service';
import { User } from '../../../users/user.model';

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
export class BlockDetailComponent implements OnInit, OnDestroy, OnChanges {
  @Input() block: BlockCompleteResponseDTO | null = null;

  private readonly destroy$ = new Subject<void>();
  currentUser: User | null = null;

  parsedContent: BlockContentItem[] = [];
  contentVisible = true;

  loading = true;
  error = false;

  tabs: Tab[] = [
    { id: 'content', label: 'blocks.detail.tabs.content' },
    { id: 'info', label: 'blocks.detail.tabs.info' },
    { id: 'versions', label: 'blocks.detail.tabs.versions' },
    { id: 'stats', label: 'blocks.detail.tabs.stats' },
  ];
  activeTabId: string = 'content';

  private modalService = inject(ModalService);
  private authState = inject(AuthStateService);

  constructor(
    private readonly route: ActivatedRoute,
    private readonly blockService: BlockService
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['block']) {
      const lastVersion = this.block?.versions?.[this.block.versions.length - 1];
      this.parsedContent = this.parseContent(lastVersion?.content);
    }
  }

  ngOnInit(): void {
    this.authState.user$.pipe(takeUntil(this.destroy$)).subscribe(u => this.currentUser = u);
    if (this.block) {
      this.loading = false;
      this.parsedContent = this.parseContent(this.block.versions?.[this.block.versions.length - 1]?.content);
    } else {
      this.route.paramMap.pipe(takeUntil(this.destroy$)).subscribe(params => {
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

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
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
        initialContent: this.parseContent(this.latestContent()),
      },
      outputs: {
        save: (data: any) => {
          const obs = isOwner
            ? this.blockService.versionBlock(data as BlockVersionCreateRequestDTO)
            : this.blockService.forkBlock({ ...data, name: this.block!.name, difficulty: this.block!.difficulty, tags: this.block!.tags, slug: '' } as BlockForkCreateRequestDTO);
          obs.subscribe({
            next: () => this.modalService.close(),
            error: () => {},
          });
        },
      },
    });
  }

  private latestContent(): string | undefined {
    return this.block?.versions?.[this.block.versions.length - 1]?.content;
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
