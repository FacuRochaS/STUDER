import { Component, Input, Output, EventEmitter, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BlockResponseDTO, BlockCompleteResponseDTO } from '../../block.model';
import { RouterModule } from '@angular/router';
import { RichTextComponent } from '../../../../shared/components/rich-text/rich-text.component';
import { TranslateModule } from '@ngx-translate/core';
import { BlockService } from '../../block.service';
import { AuthStateService } from '../../../../core/auth/auth-state.service';
import { Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'studer-block-header',
  standalone: true,
  imports: [CommonModule, RouterModule, RichTextComponent, TranslateModule],
  templateUrl: './block-header.component.html',
  styleUrls: ['./block-header.component.css']
})
export class BlockHeaderComponent implements OnInit, OnDestroy {
  @Input() block: BlockResponseDTO | BlockCompleteResponseDTO | null = null;
  @Output() likeChanged = new EventEmitter<void>();
  @Output() toggleContent = new EventEmitter<void>();
  @Output() openTree = new EventEmitter<void>();
  @Output() edit = new EventEmitter<void>();

  private readonly destroy$ = new Subject<void>();
  private blockService = inject(BlockService);
  private authState = inject(AuthStateService);
  isAuthenticated = false;
  liking = false;

  get likedByCurrentUser(): boolean { return this.block?.likedByCurrentUser ?? false; }
  get blockLikeCount(): number { return this.block?.likeCount ?? 0; }

  ngOnInit(): void {
    this.authState.user$.pipe(takeUntil(this.destroy$)).subscribe(u => this.isAuthenticated = !!u);
  }

  onToggleContent(): void {
    this.toggleContent.emit();
  }

  onOpenTree(event: MouseEvent): void {
    event.stopPropagation();
    this.openTree.emit();
  }

  onEdit(event: MouseEvent): void {
    event.stopPropagation();
    this.edit.emit();
  }

  onLike(event: MouseEvent): void {
    event.stopPropagation();
    if (!this.isAuthenticated || this.liking || !this.block || !this.block.id) return;
    this.liking = true;
    const wasLiked = this.block.likedByCurrentUser ?? false;
    const obs = wasLiked
      ? this.blockService.unlikeBlock(this.block.id)
      : this.blockService.likeBlock(this.block.id);
    obs.subscribe({
      next: () => {
        this.liking = false;
        this.block!.likedByCurrentUser = !wasLiked;
        this.block!.likeCount = (this.block!.likeCount ?? 0) + (wasLiked ? -1 : 1);
        this.likeChanged.emit();
      },
      error: () => this.liking = false
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
