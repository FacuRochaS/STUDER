import { Component, OnInit, OnDestroy, Input, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subject, takeUntil } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';
import { BlockService } from '../../block.service';
import { BlockCompleteTreeResponseDTO, BlockCompleteResponseDTO, BlockResponseDTO, BlockVersionResponseDTO } from '../../block.model';
import { LoaderComponent } from '../../../../shared/components/loader/loader.component';
import { BlockCardComponent } from '../block-card/block-card.component';
import { AuthStateService } from '../../../../core/auth/auth-state.service';
import { User } from '../../../../features/users/user.model';

interface TreeNode {
  id: number; name: string; slug: string; difficulty: string;
  ownerName: string; ownerId: number; isFork: boolean;
  likeCount: number; tags: string[];
  versions: BlockVersionResponseDTO[];
  parent: TreeNode | null; children: TreeNode[];
  depth: number; index: number; x: number; y: number;
  versionY: number; fetched: boolean;
}

@Component({
  selector: 'studer-block-tree',
  standalone: true,
  imports: [CommonModule, TranslateModule, LoaderComponent, BlockCardComponent],
  templateUrl: './block-tree.component.html',
  styleUrls: ['./block-tree.component.css']
})
export class BlockTreeComponent implements OnInit, OnDestroy {
  @Input() blockId!: number;
  private destroy$ = new Subject<void>();
  private blockService = inject(BlockService);
  private authState = inject(AuthStateService);

  currentUser: User | null = null;
  tree: { nodes: TreeNode[]; width: number; height: number } | null = null;
  loading = true; error = false;
  zoom = 1; panX = 0; panY = 0;
  private isPanning = false; private startX = 0; private startY = 0;
  selectedNode: TreeNode | null = null;
  selectedBlock: BlockResponseDTO | BlockCompleteResponseDTO | null = null;
  selectedVersionId: number | null = null;

  readonly NW = 180; readonly NH = 44;
  readonly VH = 26; readonly GX = 50; readonly GY = 12;

  ngOnInit(): void {
    this.authState.user$.pipe(takeUntil(this.destroy$)).subscribe(u => this.currentUser = u);
    if (this.blockId) this.loadTree(this.blockId); else this.loading = false;
  }

  loadTree(id: number): void {
    this.loading = true; this.error = false;
    this.selectedNode = null; this.selectedBlock = null;
    this.blockService.getBlockTree(id).pipe(takeUntil(this.destroy$)).subscribe({
      next: (data) => {
        this.tree = this.buildLayout(data);
        this.loading = false;
        setTimeout(() => this.fitView(), 50);
        const center = this.tree?.nodes.find(n => n.id === id) || this.tree?.nodes[0];
        if (center) this.selectNode(center);
      },
      error: () => { this.loading = false; this.error = true; }
    });
  }

  private buildLayout(data: BlockCompleteTreeResponseDTO) {
    const nodes: TreeNode[] = [];
    const toNode = (b: any, depth: number, idx: number, fetched: boolean): TreeNode => ({
      id: b.id, name: b.name, slug: b.slug, difficulty: b.difficulty || 'NORMAL',
      ownerName: b.owner?.username || '?', ownerId: b.owner?.id || 0,
      isFork: b.isFork || false, likeCount: b.likeCount ?? 0,
      tags: b.tags || [], versions: (b.versions || (b.version ? [b.version] : [])).slice().sort((a: any, b: any) => b.versionNumber - a.versionNumber),
      parent: null, children: [], depth, index: idx, x: 0, y: 0, versionY: 0, fetched
    });

    const current = toNode(data.block, 0, 0, true);
    nodes.push(current);

    for (let i = data.parents.length - 1; i >= 0; i--) {
      const p = toNode(data.parents[i], -(data.parents.length - i), 0, true);
      p.children = [current]; current.parent = p; nodes.push(p);
    }
    data.sons.forEach((s: any, i: number) => {
      const child = toNode(s, 1, i + 1, false);
      current.children.push(child); child.parent = current; nodes.push(child);
    });

    // Calculate positions: parents left-to-right at depth < 0, current at 0, forks at depth > 0
    const minD = Math.min(...nodes.map(n => n.depth));
    const maxD = Math.max(...nodes.map(n => n.depth));
    const cols: TreeNode[][] = [];
    for (let d = minD; d <= maxD; d++) {
      cols[d - minD] = nodes.filter(n => n.depth === d).sort((a, b) => a.index - b.index);
    }
    let maxH = 0;
    cols.forEach((col, ci) => {
      let y = 20;
      col.forEach(node => {
        node.x = ci * (this.NW + this.GX) + 40;
        node.y = y;
        const vCount = Math.max(node.versions.length, 1);
        node.versionY = vCount * this.VH;
        y += this.NH + node.versionY + this.GY;
      });
      maxH = Math.max(maxH, y);
    });
    const totalW = cols.length * (this.NW + this.GX) + 40;
    return { nodes, width: totalW, height: maxH + 40 };
  }

  selectNode(node: TreeNode): void {
    this.selectedNode = node;
    this.selectedBlock = null;
    this.selectedVersionId = null;
    this.blockService.getBlockVersion(node.id).pipe(takeUntil(this.destroy$)).subscribe({
      next: (data) => { this.selectedBlock = data; },
    });
  }

  selectVersion(node: TreeNode, v: BlockVersionResponseDTO, event: MouseEvent): void {
    event.stopPropagation();
    this.selectedNode = node;
    this.selectedVersionId = v.id;
    this.blockService.getBlock(node.id).pipe(takeUntil(this.destroy$)).subscribe({
      next: (data) => {
        // Build a modified response with only the selected version
        this.selectedBlock = { ...data, version: v, versions: [v] } as any;
      },
    });
  }

  onNodeClick(node: TreeNode): void { this.selectNode(node); }
  fitView(): void { if (!this.tree) return; this.zoom = Math.min(500 / this.tree.width, 350 / this.tree.height, 1.5); this.panX = 20; this.panY = 10; }
  zoomIn(): void { this.zoom = Math.min(this.zoom + 0.25, 3); }
  zoomOut(): void { this.zoom = Math.max(this.zoom - 0.25, 0.3); }
  resetView(): void { this.fitView(); }
  onWheel(e: WheelEvent): void { e.preventDefault(); this.zoom = Math.max(0.3, Math.min(3, this.zoom + (e.deltaY > 0 ? -0.1 : 0.1))); }
  onMouseDown(e: MouseEvent): void { if ((e.target as HTMLElement).closest('button, .tree-node, .ver-pill')) return; this.isPanning = true; this.startX = e.clientX - this.panX; this.startY = e.clientY - this.panY; }
  onMouseMove(e: MouseEvent): void { if (!this.isPanning) return; this.panX = e.clientX - this.startX; this.panY = e.clientY - this.startY; }
  onMouseUp(): void { this.isPanning = false; }
  isInPath(node: TreeNode): boolean { if (!this.selectedNode) return false; let c: TreeNode | null = this.selectedNode; while (c) { if (c.id === node.id) return true; c = c.parent; } return false; }
  trackById(_: number, node: TreeNode): number { return node.id; }
  trackByV(_: number, v: BlockVersionResponseDTO): number { return v.id; }
  ngOnDestroy(): void { this.destroy$.next(); this.destroy$.complete(); }
}
