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
  @Input() likedByCurrentUser = false;
  @Input() blockLikeCount = 0;
  @Output() likeChanged = new EventEmitter<void>();

  private readonly destroy$ = new Subject<void>();
  private blockService = inject(BlockService);
  private authState = inject(AuthStateService);
  isAuthenticated = false;
  liking = false;

  ngOnInit(): void {
    this.authState.user$.pipe(takeUntil(this.destroy$)).subscribe(u => this.isAuthenticated = !!u);
  }

  toggleLike(): void {
    if (!this.isAuthenticated || this.liking || !this.block) return;
    this.liking = true;
    const obs = this.likedByCurrentUser
      ? this.blockService.unlikeBlock(this.block.id)
      : this.blockService.likeBlock(this.block.id);
    obs.subscribe({
      next: () => { this.liking = false; this.likeChanged.emit(); },
      error: () => this.liking = false
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
