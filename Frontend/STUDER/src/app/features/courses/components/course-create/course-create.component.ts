import { Component, EventEmitter, Input, OnDestroy, OnInit, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, takeUntil, debounceTime, distinctUntilChanged, switchMap, of } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';
import { Router, RouterModule } from '@angular/router';
import { CourseService } from '../../course.service';
import { ContestService } from '../../../contest/contest.service';
import { CourseCreateRequestDTO, CourseUpdateRequestDTO, CourseBlockRequestDTO, CourseResponseDTO } from '../../course.model';
import { BlockService } from '../../../blocks/block.service';
import { BlockResponseDTO, BlockPageResponseDTO, BlockVersionCreateRequestDTO, BlockForkCreateRequestDTO } from '../../../blocks/block.model';
import { TagInputComponent } from '../../../../shared/components/tag-input/tag-input.component';
import { ModalService } from '../../../../shared/services/modal.service';
import { BlockEditorComponent } from '../../../blocks/editor/block-editor.component';
import { BlockHeaderComponent } from '../../../blocks/component/block-header/block-header.component';
import { BlockViewerComponent } from '../../../blocks/component/block-viewer/block-viewer.component';
import { BlockContentItem } from '../../../blocks/interfaces/content.interfaces';
import { AuthStateService } from '../../../../core/auth/auth-state.service';
import { User } from '../../../users/user.model';
import { UploadService } from '../../../../core/services/upload.service';

interface SelectedBlock {
  blockId: number;
  blockName: string;
  order: number;
  keepUpdated: boolean;
  versionId: number | null;
  blockData: BlockResponseDTO;
}

@Component({
  selector: 'studer-course-create',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TranslateModule,
    RouterModule,
    TagInputComponent,
    BlockHeaderComponent,
    BlockViewerComponent,
  ],
  templateUrl: './course-create.component.html',
  styleUrls: ['./course-create.component.css'],
})
export class CourseCreateComponent implements OnInit, OnDestroy {
  @Output() saved = new EventEmitter<number>();

  @Input() mode: 'create' | 'edit' = 'create';
  @Input() editCourse: CourseResponseDTO | null = null;
  @Input() contestId: number | null = null;

  private readonly destroy$ = new Subject<void>();
  private readonly courseService = inject(CourseService);
  private readonly contestService = inject(ContestService);
  private readonly blockService = inject(BlockService);
  private readonly modalService = inject(ModalService);
  private readonly router = inject(Router);
  private readonly authState = inject(AuthStateService);
  private readonly uploadService = inject(UploadService);

  currentUser: User | null = null;
  currentStep: 1 | 2 = 1;
  expandedBlocks = new Set<number>();

  name = '';
  tags: string[] = [];
  imageFile: File | null = null;
  imagePreviewUrl: string | null = null;

  selectedBlocks: SelectedBlock[] = [];
  saving = false;

  dragIndex: number | null = null;

  blockBrowserOpen = false;
  blockSearchQuery = '';
  blockSearchResults: BlockResponseDTO[] = [];
  blockSearchLoading = false;
  blockSearchPage = 0;
  blockSearchHasMore = false;
  blockFilterDifficulty = '';
  blockFilterOwnOnly = false;
  blockFilterFollowing = false;
  blockFilterLiked = false;
  blockFilterMostLiked = false;
  blockFilterTags: string[] = [];

  blockSearchPageSize = 20;
  loadingMoreBlocks = false;

  draggedBrowserBlock: BlockResponseDTO | null = null;

  private readonly blockSearchSubject = new Subject<void>();

  ngOnInit(): void {
    this.authState.user$.pipe(takeUntil(this.destroy$)).subscribe(u => this.currentUser = u);

    if (this.mode === 'edit' && this.editCourse) {
      this.name = this.editCourse.name;
      this.tags = [...this.editCourse.tags];
      this.currentStep = 2;

      if (this.editCourse.link) {
        this.imagePreviewUrl = this.editCourse.link;
      }

      this.selectedBlocks = this.editCourse.blocks.map((b, i) => ({
        blockId: b.blockId,
        blockName: b.blockName,
        order: b.order,
        keepUpdated: true,
        versionId: b.version?.id ?? null,
        blockData: {
          id: b.blockId,
          name: b.blockName,
          slug: '',
          difficulty: '' as any,
          tags: [],
          version: b.version,
          owner: {
            id: b.blockId,
            username: '',
            firstName: '',
            lastName: '',
            profilePictureOriginalUrl: undefined,
            profilePictureAvatarUrl: undefined,
            profilePictureWebpUrl: undefined,
            profilePictureThumbnailUrl: undefined,
          },
          likeCount: 0,
          likedByCurrentUser: false,
          isFork: false,
          createdDatetime: '',
          lastUpdatedDatetime: '',
        } as unknown as BlockResponseDTO,
      }));
    }

    this.blockSearchSubject.pipe(
      debounceTime(300),
      switchMap(() => {
        this.blockSearchLoading = true;
        return this.blockService.exploreBlocks(0, {
          query: this.blockSearchQuery.trim() || undefined,
          tags: this.blockFilterTags.length > 0 ? this.blockFilterTags : undefined,
          difficulty: this.blockFilterDifficulty || undefined,
          mine: this.blockFilterOwnOnly || undefined,
          following: this.blockFilterFollowing || undefined,
          liked: this.blockFilterLiked || undefined,
        });
      }),
      takeUntil(this.destroy$),
    ).subscribe((page: BlockPageResponseDTO | null) => {
      if (page) {
        this.blockSearchResults = page.blocks;
        this.blockSearchHasMore = page.hasMore;
        this.blockSearchPage = 0;
      }
      this.blockSearchLoading = false;
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    if (file) {
      this.imageFile = file;
      this.imagePreviewUrl = URL.createObjectURL(file);
    }
  }

  removeImage(): void {
    this.imageFile = null;
    this.imagePreviewUrl = null;
  }

  goToStep(step: 1 | 2): void {
    this.currentStep = step;
  }

  selectBlockFromBrowser(block: BlockResponseDTO): void {
    if (this.selectedBlocks.some(b => b.blockId === block.id)) return;

    this.selectedBlocks = [...this.selectedBlocks, {
      blockId: block.id,
      blockName: block.name,
      order: this.selectedBlocks.length + 1,
      keepUpdated: true,
      versionId: null,
      blockData: block,
    }];
    this.updateOrders();
  }

  removeBlock(index: number): void {
    this.selectedBlocks = this.selectedBlocks.filter((_, i) => i !== index);
    this.expandedBlocks.delete(index);
    this.updateOrders();
  }

  toggleBlockContent(index: number): void {
    if (this.expandedBlocks.has(index)) {
      this.expandedBlocks.delete(index);
    } else {
      this.expandedBlocks.add(index);
    }
  }

  parseContent(content: string | undefined): BlockContentItem[] {
    if (!content) return [];
    try {
      return JSON.parse(content);
    } catch {
      return [];
    }
  }

  onKeepUpdatedChange(index: number): void {
    const b = this.selectedBlocks[index];
    if (b.keepUpdated) {
      b.versionId = null;
    } else {
      b.versionId = b.blockData.version?.id ?? null;
    }
  }

  onDragStart(index: number): void {
    this.dragIndex = index;
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
  }

  onDrop(index: number): void {
    if (this.draggedBrowserBlock) {
      this.selectBlockFromBrowser(this.draggedBrowserBlock);
      this.draggedBrowserBlock = null;
      return;
    }
    if (this.dragIndex === null || this.dragIndex === index) return;

    const blocks = [...this.selectedBlocks];
    const [moved] = blocks.splice(this.dragIndex, 1);
    blocks.splice(index, 0, moved);
    this.selectedBlocks = blocks;
    this.updateOrders();
    this.dragIndex = null;
  }

  onDragEnd(): void {
    this.dragIndex = null;
    this.draggedBrowserBlock = null;
  }

  onBrowserDragStart(block: BlockResponseDTO): void {
    this.draggedBrowserBlock = block;
  }

  onDropFromBrowser(event: DragEvent): void {
    event.preventDefault();
    if (this.draggedBrowserBlock) {
      this.selectBlockFromBrowser(this.draggedBrowserBlock);
      this.draggedBrowserBlock = null;
    }
  }

  loadMoreBlocks(): void {
    if (this.loadingMoreBlocks || !this.blockSearchHasMore) return;

    this.loadingMoreBlocks = true;
    const nextPage = this.blockSearchPage + 1;
    const query = this.blockSearchQuery.trim();

    this.blockService.getBlocksBySearch(
      nextPage,
      this.blockFilterTags.length > 0 ? this.blockFilterTags : undefined,
      this.blockFilterMostLiked || undefined,
      this.blockFilterDifficulty || undefined,
      undefined,
      query.length >= 2 ? query : undefined,
    ).subscribe({
      next: (page) => {
        this.blockSearchResults = [...this.blockSearchResults, ...page.blocks];
        this.blockSearchHasMore = page.hasMore;
        this.blockSearchPage = nextPage;
        this.loadingMoreBlocks = false;
      },
      error: () => {
        this.loadingMoreBlocks = false;
      },
    });
  }

  onBlockSearchInput(): void {
    this.blockSearchSubject.next();
  }

  toggleFilterOwnOnly(): void { this.blockFilterOwnOnly = !this.blockFilterOwnOnly; this.blockFilterFollowing = false; this.blockFilterLiked = false; this.blockSearchSubject.next(); }
  toggleFilterFollowing(): void { this.blockFilterFollowing = !this.blockFilterFollowing; this.blockFilterOwnOnly = false; this.blockFilterLiked = false; this.blockSearchSubject.next(); }
  toggleFilterLiked(): void { this.blockFilterLiked = !this.blockFilterLiked; this.blockFilterOwnOnly = false; this.blockFilterFollowing = false; this.blockSearchSubject.next(); }

  toggleFilterMostLiked(): void {
    this.blockFilterMostLiked = !this.blockFilterMostLiked;
    this.blockSearchSubject.next();
  }

  setBlockFilterDifficulty(difficulty: string): void {
    this.blockFilterDifficulty = this.blockFilterDifficulty === difficulty ? '' : difficulty;
    this.blockSearchSubject.next();
  }

  openBlockEditor(): void {
    this.modalService.open(BlockEditorComponent, {
      title: 'course.create.create_new_block',
      inputs: { initialContent: [] },
      outputs: {
        save: (data: any) => {
          this.blockService.createBlock(data).subscribe({
            next: (created: BlockResponseDTO) => {
              this.modalService.close();
              this.selectBlockFromBrowser(created);
            },
          });
        },
      },
    });
  }

  onEditCourseBlock(index: number): void {
    const block = this.selectedBlocks[index]?.blockData;
    if (!block) return;
    const isOwner = this.currentUser?.id === block.owner.id;
    this.modalService.open(BlockEditorComponent, {
      title: isOwner ? 'blocks.editor.actions.edit' : 'blocks.editor.actions.fork',
      inputs: {
        mode: 'edit',
        blockId: block.id,
        blockName: block.name,
        blockDifficulty: block.difficulty,
        blockTags: block.tags,
        initialContent: this.parseContent(block.version?.content),
      },
      outputs: {
        save: (data: any) => {
          const obs = isOwner
            ? this.blockService.versionBlock(data as BlockVersionCreateRequestDTO)
            : this.blockService.forkBlock({ ...data, name: block.name, difficulty: block.difficulty, tags: block.tags, blockId: block.id, slug: '' } as BlockForkCreateRequestDTO);
          obs.subscribe({
            next: (updated: BlockResponseDTO) => {
              this.modalService.close();
              const entry = this.selectedBlocks[index];
              if (entry) {
                entry.blockData = updated;
                entry.blockId = updated.id;
                entry.versionId = updated.version?.id ?? null;
              }
            },
            error: () => {},
          });
        },
      },
    });
  }

  onSave(): void {
    if (this.mode !== 'edit' && !this.name.trim()) return;

    this.saving = true;
    const blocks: CourseBlockRequestDTO[] = this.selectedBlocks
      .filter(b => b.blockId != null)
      .map(b => ({
        blockId: b.blockId,
        versionId: b.keepUpdated ? null : b.blockData.version?.id ?? null,
        order: b.order,
      }));

    if (this.mode === 'edit' && this.editCourse) {
      const request: CourseUpdateRequestDTO = { blocks };
      this.courseService.update(this.editCourse.id, request).subscribe({
        next: (course) => {
          this.saving = false;
          this.saved.emit(course.id);
        },
        error: () => {
          this.saving = false;
        },
      });
    } else {
      const request: CourseCreateRequestDTO = {
        name: this.name.trim(),
        tags: this.tags.length > 0 ? this.tags : undefined,
        blocks,
      };

      if (this.imageFile) {
        this.uploadService.uploadImage(this.imageFile, 'courses').subscribe({
          next: (res) => { request.link = res.url; this.doCreate(request); },
          error: () => this.doCreate(request),
        });
      } else {
        this.doCreate(request);
      }
    }
  }

  private doCreate(request: CourseCreateRequestDTO): void {
    if (this.contestId) {
      this.contestService.submitCourse(this.contestId, request).subscribe({
        next: (course) => { this.saving = false; this.saved.emit(course.id); },
        error: () => { this.saving = false; },
      });
    } else {
      this.courseService.create(request).subscribe({
        next: (course) => {
          this.saving = false;
          this.saved.emit(course.id);
          this.router.navigate(['/courses']);
        },
        error: () => { this.saving = false; },
      });
    }
  }

  private updateOrders(): void {
    this.selectedBlocks = this.selectedBlocks.map((b, i) => ({ ...b, order: i + 1 }));
  }

  trackByBlockId(index: number, block: SelectedBlock): number {
    return block.blockId;
  }

  trackBySearchResult(index: number, result: BlockResponseDTO): number {
    return result.id;
  }
}
