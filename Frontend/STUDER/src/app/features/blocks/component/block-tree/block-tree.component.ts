import { Component, OnInit, OnDestroy, Input, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';
import { BlockService } from '../../block.service';
import { BlockCompleteTreeResponseDTO } from '../../block.model';
import { LoaderComponent } from '../../../../shared/components/loader/loader.component';

@Component({
  selector: 'studer-block-tree',
  standalone: true,
  imports: [CommonModule, RouterModule, TranslateModule, LoaderComponent],
  template: `
    <div class="block-tree">
      @if (loading) {
        <studer-loader></studer-loader>
      } @else if (treeData) {
        <div class="tree">
          <div class="tree__node tree__node--parent">
            <a [routerLink]="['/blocks', treeData.parent?.id]">
              {{ treeData.parent?.name || 'Parent' }}
            </a>
          </div>
          <div class="tree__connector"></div>
          <div class="tree__node tree__node--current">
            <a [routerLink]="['/blocks', treeData.block?.id]">
              {{ treeData.block?.name || 'Current' }}
            </a>
            <span class="tree__badge">{{ 'block.current' | translate }}</span>
          </div>
          @if (treeData.sons?.length) {
            <div class="tree__connector"></div>
            <div class="tree__children">
              @for (son of treeData.sons; track son.id) {
                <div class="tree__node tree__node--child">
                  <a [routerLink]="['/blocks', son.id]">{{ son.name }}</a>
                </div>
              }
            </div>
          }
        </div>
      } @else {
        <div class="tree__empty">No tree data available</div>
      }
    </div>
  `,
  styles: [`
    .block-tree { padding: 1rem; }
    .tree { display: flex; flex-direction: column; align-items: center; gap: 0.5rem; }
    .tree__node { padding: 0.5rem 1rem; border-radius: 8px; background: var(--color-bg); box-shadow: 0 1px 3px var(--box-shadow-color); }
    .tree__node a { color: var(--color-primary); text-decoration: none; font-weight: 500; }
    .tree__node a:hover { text-decoration: underline; }
    .tree__node--current { border: 2px solid var(--color-primary); }
    .tree__badge { display: inline-block; margin-left: 0.5rem; padding: 0.1rem 0.4rem; font-size: 0.7rem; background: var(--color-primary); color: #fff; border-radius: 4px; }
    .tree__connector { width: 2px; height: 20px; background: var(--border); }
    .tree__children { display: flex; gap: 0.5rem; flex-wrap: wrap; justify-content: center; }
    .tree__empty { text-align: center; color: var(--color-text-secu); padding: 2rem; }
  `]
})
export class BlockTreeComponent implements OnInit, OnDestroy {
  @Input() blockId!: number;
  private readonly destroy$ = new Subject<void>();
  private blockService = inject(BlockService);

  treeData: BlockCompleteTreeResponseDTO | null = null;
  loading = true;

  ngOnInit(): void {
    if (this.blockId) {
      this.blockService.getBlockTree(this.blockId).pipe(takeUntil(this.destroy$)).subscribe({
        next: (data) => { this.treeData = data; this.loading = false; },
        error: () => this.loading = false
      });
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
