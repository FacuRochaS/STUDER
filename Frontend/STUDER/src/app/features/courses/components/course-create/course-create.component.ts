import { Component, EventEmitter, OnDestroy, OnInit, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, takeUntil, debounceTime, distinctUntilChanged } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';
import { Router, RouterModule } from '@angular/router';
import { CourseService } from '../../course.service';
import { CourseCreateRequestDTO, CourseBlockRequestDTO } from '../../course.model';
import { BlockService } from '../../../blocks/block.service';
import { BlockResponseDTO, BlockVersionResponseDTO, BlockPageResponseDTO, BlockCompleteResponseDTO } from '../../../blocks/block.model';
import { TagInputComponent } from '../../../../shared/components/tag-input/tag-input.component';
import { ModalService } from '../../../../shared/services/modal.service';
import { BlockEditorComponent } from '../../../blocks/editor/block-editor.component';

interface SelectedBlock {
  blockId: number;
  blockName: string;
  versionId: number | null;
  order: number;
  versions: BlockVersionResponseDTO[];
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
  ],
  templateUrl: './course-create.component.html',
  styleUrls: ['./course-create.component.css'],
})
export class CourseCreateComponent implements OnInit, OnDestroy {
  @Output() saved = new EventEmitter<number>();

  private readonly destroy$ = new Subject<void>();

  private readonly courseService = inject(CourseService);
  private readonly blockService = inject(BlockService);
  private readonly modalService = inject(ModalService);
  private readonly router = inject(Router);

  name = '';
  tags: string[] = [];
  imageFile: File | null = null;
  imagePreviewUrl: string | null = null;

  searchQuery = '';
  searchResults: BlockResponseDTO[] = [];
  searchLoading = false;
  showSearchResults = false;

  selectedBlocks: SelectedBlock[] = [];
  saving = false;

  dragIndex: number | null = null;

  private readonly searchSubject = new Subject<string>();

  ngOnInit(): void {
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntil(this.destroy$),
    ).subscribe(query => {
      if (query.trim().length < 2) {
        this.searchResults = [];
        this.searchLoading = false;
        return;
      }
      this.searchLoading = true;
      this.blockService.getBlocksBySearch(0, undefined, undefined, undefined, undefined, query.trim()).subscribe({
        next: (page: BlockPageResponseDTO) => {
          this.searchResults = page.blocks;
          this.searchLoading = false;
        },
        error: () => {
          this.searchResults = [];
          this.searchLoading = false;
        },
      });
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

  onSearchInput(): void {
    this.searchSubject.next(this.searchQuery);
    this.showSearchResults = true;
  }

  onSearchBlur(): void {
    setTimeout(() => this.showSearchResults = false, 200);
  }

  selectBlock(block: BlockResponseDTO): void {
    if (this.selectedBlocks.some(b => b.blockId === block.id)) return;

    this.blockService.getBlockVersion(block.id).subscribe({
      next: (full: BlockCompleteResponseDTO) => {
        this.selectedBlocks = [...this.selectedBlocks, {
          blockId: block.id,
          blockName: full.name,
          versionId: null,
          order: this.selectedBlocks.length + 1,
          versions: full.versions,
        }];
        this.updateOrders();
        this.searchQuery = '';
        this.searchResults = [];
        this.showSearchResults = false;
      },
    });
  }

  removeBlock(index: number): void {
    this.selectedBlocks = this.selectedBlocks.filter((_, i) => i !== index);
    this.updateOrders();
  }

  setVersion(index: number, versionId: string): void {
    this.selectedBlocks[index].versionId = versionId ? Number(versionId) : null;
  }

  onDragStart(index: number): void {
    this.dragIndex = index;
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
  }

  onDrop(index: number): void {
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
  }

  openBlockEditor(): void {
    this.modalService.open(BlockEditorComponent, {
      title: 'course.create.new_block_modal_title',
      inputs: { initialContent: [] },
      outputs: {
        save: (data: any) => {
          this.blockService.createBlock(data).subscribe({
            next: (created: BlockResponseDTO) => {
              this.modalService.close();
              this.selectBlock(created);
            },
          });
        },
      },
    });
  }

  onSave(): void {
    if (!this.name.trim()) return;

    this.saving = true;
    const blocks: CourseBlockRequestDTO[] = this.selectedBlocks.map(b => ({
      blockId: b.blockId,
      versionId: b.versionId,
      order: b.order,
    }));

    const request: CourseCreateRequestDTO = {
      name: this.name.trim(),
      tags: this.tags.length > 0 ? this.tags : undefined,
      blocks,
    };

    this.courseService.create(request, this.imageFile ?? undefined).subscribe({
      next: (course) => {
        this.saving = false;
        this.saved.emit(course.id);
        this.router.navigate(['/courses', course.id]);
      },
      error: () => {
        this.saving = false;
      },
    });
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
