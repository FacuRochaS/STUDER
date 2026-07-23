import { Component, OnInit, OnDestroy, Input, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subject, takeUntil } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';
import { BlockService } from '../../block.service';
import {
  BlockCompleteTreeResponseDTO, BlockCompleteResponseDTO, BlockResponseDTO, BlockVersionResponseDTO,
  BlockVersionCreateRequestDTO, BlockForkCreateRequestDTO
} from '../../block.model';
import { LoaderComponent } from '../../../../shared/components/loader/loader.component';
import { BlockDetailComponent } from '../block-detail/block-detail.component';
import { BlockContentItem } from '../../interfaces/content.interfaces';
import { ModalService } from '../../../../shared/services/modal.service';
import { BlockEditorComponent } from '../../editor/block-editor.component';
import { AuthStateService } from '../../../../core/auth/auth-state.service';
import { User } from '../../../../features/users/user.model';

interface TreeNode {
  id: number;
  name: string;
  slug: string;
  difficulty: string;
  ownerName: string;
  ownerId: number;
  isFork: boolean;
  isOriginal: boolean;
  likeCount: number;
  likedByCurrentUser: boolean;
  tags: string[];
  versions: BlockVersionResponseDTO[];
  parent: TreeNode | null;
  children: TreeNode[];
  depth: number;
  index: number;
  x: number;
  y: number;
  fetched: boolean;
}

interface TreeLayout {
  nodes: TreeNode[];
  width: number;
  height: number;
}

@Component({
  selector: 'studer-block-tree',
  standalone: true,
  imports: [CommonModule, TranslateModule, LoaderComponent, BlockDetailComponent],
  templateUrl: './block-tree.component.html',
  styleUrls: ['./block-tree.component.css']
})
export class BlockTreeComponent implements OnInit, OnDestroy {
  @Input() blockId!: number;
  private readonly destroy$ = new Subject<void>();
  private blockService = inject(BlockService);
  private modalService = inject(ModalService);
  private authState = inject(AuthStateService);

  currentUser: User | null = null;
  tree: TreeLayout | null = null;
  loading = true;
  error = false;

  zoom = 1;
  panX = 0;
  panY = 0;
  private isPanning = false;
  private startX = 0;
  private startY = 0;

  selectedNode: TreeNode | null = null;
  selectedBlockDetail: BlockCompleteResponseDTO | null = null;
  selectedVersion: BlockVersionResponseDTO | null = null;
  hoveredNode: TreeNode | null = null;
  hoveredVersion: BlockVersionResponseDTO | null = null;
  tooltipPos = { x: 0, y: 0 };

  readonly NODE_WIDTH = 220;
  readonly NODE_GAP_X = 60;
  readonly NODE_GAP_Y = 20;
  readonly VERSION_HEIGHT = 32;

  ngOnInit(): void {
    this.authState.user$.pipe(takeUntil(this.destroy$)).subscribe(u => this.currentUser = u);
    if (this.blockId) {
      this.loadTree(this.blockId);
    } else {
      this.loading = false;
    }
  }

  loadTree(id: number): void {
    this.loading = true;
    this.error = false;
    this.selectedNode = null;
    this.selectedVersion = null;
    this.zoom = 1;
    this.panX = 0;
    this.panY = 0;
    this.blockService.getBlockTree(id).pipe(takeUntil(this.destroy$)).subscribe({
      next: (data) => {
        this.tree = this.buildLayout(data);
        this.loading = false;
        if (this.tree && this.tree.nodes.length > 0) {
          const center = this.tree.nodes.find(n => n.id === id) || this.tree.nodes[0];
          this.selectNode(center);
        }
      },
      error: () => {
        this.loading = false;
        this.error = true;
      }
    });
  }

  private buildLayout(data: BlockCompleteTreeResponseDTO): TreeLayout {
    const nodeMap = new Map<number, TreeNode>();
    const roots: TreeNode[] = [];

    const toNode = (b: BlockCompleteResponseDTO, depth: number, idx: number): TreeNode => {
      const n: TreeNode = {
        id: b.id, name: b.name, slug: b.slug, difficulty: b.difficulty,
        ownerName: b.owner.username, ownerId: b.owner.id,
        isFork: b.isFork, isOriginal: !b.isFork && (!b.parent || data.parents.length === 0),
        likeCount: b.likeCount ?? 0, likedByCurrentUser: b.likedByCurrentUser ?? false,
        tags: b.tags, versions: [...b.versions].sort((a, bb) => a.versionNumber - bb.versionNumber),
        parent: null, children: [], depth, index: idx, x: 0, y: 0, fetched: true
      };
      nodeMap.set(n.id, n);
      return n;
    };

    const toNodeFromResponse = (b: BlockResponseDTO, depth: number, idx: number): TreeNode => ({
      id: b.id, name: b.name, slug: b.slug, difficulty: b.difficulty,
      ownerName: b.owner.username, ownerId: b.owner.id,
      isFork: b.isFork, isOriginal: false,
      likeCount: b.likeCount ?? 0, likedByCurrentUser: b.likedByCurrentUser ?? false,
      tags: b.tags, versions: b.version ? [b.version] : [],
      parent: null, children: [], depth, index: idx, x: 0, y: 0, fetched: false
    });

    const current = toNode(data.block, 0, 0);
    roots.push(current);

    for (let i = data.parents.length - 1; i >= 0; i--) {
      const p = toNode(data.parents[i], -(data.parents.length - i), 0);
      p.children = [current];
      current.parent = p;
      roots[0] = p;
    }

    data.sons.forEach((s, i) => {
      const child = toNodeFromResponse(s, 0, i + 1);
      current.children.push(child);
      child.parent = current;
      roots.push(child);
    });

    this.fetchChildren(roots);

    return this.calculateLayout(roots);
  }

  private fetchChildren(roots: TreeNode[]): void {
    const toFetch = roots.filter(r => !r.fetched && r.id);
    toFetch.forEach(n => {
      this.blockService.getBlock(n.id).pipe(takeUntil(this.destroy$)).subscribe({
        next: (full) => {
          n.fetched = true;
          n.difficulty = full.difficulty;
          n.likeCount = full.likeCount ?? 0;
          n.likedByCurrentUser = full.likedByCurrentUser ?? false;
          n.tags = full.tags;
          n.slug = full.slug;
          if (full.version) {
            const exists = n.versions.some(v => v.id === full.version!.id);
            if (!exists) n.versions.push(full.version);
            n.versions.sort((a, b) => a.versionNumber - b.versionNumber);
          }
        }
      });
    });
  }

  private calculateLayout(roots: TreeNode[]): TreeLayout {
    const allNodes: TreeNode[] = [];
    const levels: TreeNode[][] = [];

    const collect = (node: TreeNode, depth: number) => {
      if (!levels[depth + 10]) levels[depth + 10] = [];
      levels[depth + 10].push(node);
      allNodes.push(node);
      node.depth = depth;
      node.children.forEach(c => collect(c, depth + 1));
    };

    roots.forEach(r => collect(r, r.depth));
    if (allNodes.length === 0) return { nodes: [], width: 0, height: 0 };

    const minDepth = Math.min(...allNodes.map(n => n.depth));
    levels.forEach((level, di) => {
      level.sort((a, b) => a.index - b.index);
      level.forEach((node, li) => {
        const depthOffset = node.depth - minDepth;
        let maxVersionH = Math.max(node.versions.length, 1) * this.VERSION_HEIGHT;
        node.x = depthOffset * (this.NODE_WIDTH + this.NODE_GAP_X);
        if (li > 0) {
          const prev = level[li - 1];
          node.y = prev.y + Math.max(prev.versions.length, 1) * this.VERSION_HEIGHT + this.NODE_GAP_Y;
        } else {
          node.y = 0;
        }
      });
    });

    const totalW = (Math.max(...allNodes.map(n => n.depth)) - minDepth + 1) * (this.NODE_WIDTH + this.NODE_GAP_X) + this.NODE_GAP_X;
    const maxY = Math.max(...allNodes.map(n => n.y + n.versions.length * this.VERSION_HEIGHT));
    return { nodes: allNodes, width: totalW, height: maxY + 80 };
  }

  selectNode(node: TreeNode): void {
    this.selectedNode = node;
    this.selectedBlockDetail = null;
    this.selectedVersion = node.versions.length > 0 ? node.versions[node.versions.length - 1] : null;
    this.detailTab = 'info';
    this.blockService.getBlockVersion(node.id).pipe(takeUntil(this.destroy$)).subscribe({
      next: (full) => {
        this.selectedBlockDetail = full;
        node.fetched = true;
        node.difficulty = full.difficulty;
        node.likeCount = full.likeCount ?? 0;
        node.likedByCurrentUser = full.likedByCurrentUser ?? false;
        node.tags = full.tags;
        node.slug = full.slug;
      },
    });
  }

  selectVersion(v: BlockVersionResponseDTO): void {
    this.selectedVersion = v;
  }

  onNodeClick(node: TreeNode): void {
    this.selectNode(node);
  }

  onNodeHover(event: MouseEvent, node: TreeNode): void {
    this.hoveredNode = node;
    this.hoveredVersion = node.versions.length > 0 ? node.versions[node.versions.length - 1] : null;
    this.tooltipPos = { x: event.clientX, y: event.clientY };
  }

  onNodeLeave(): void {
    this.hoveredNode = null;
    this.hoveredVersion = null;
  }

  onCanvasDblClick(): void {
    if (this.selectedNode) {
      this.panX = -(this.selectedNode.x * this.zoom) + 200;
      this.panY = -(this.selectedNode.y * this.zoom) + 100;
    }
  }

  fitView(): void {
    if (!this.tree || this.tree.nodes.length === 0) return;
    const vw = 700;
    const vh = 400;
    const scaleX = vw / this.tree.width;
    const scaleY = vh / this.tree.height;
    this.zoom = Math.min(scaleX, scaleY, 1.5);
    this.panX = 40;
    this.panY = 20;
  }

  centerView(): void {
    if (this.selectedNode) {
      this.panX = -(this.selectedNode.x * this.zoom) + 200;
      this.panY = -(this.selectedNode.y * this.zoom) + 100;
    }
  }

  zoomIn(): void {
    this.zoom = Math.min(this.zoom + 0.25, 3);
  }

  zoomOut(): void {
    this.zoom = Math.max(this.zoom - 0.25, 0.3);
  }

  resetView(): void {
    this.fitView();
  }

  onWheel(event: WheelEvent): void {
    event.preventDefault();
    const delta = event.deltaY > 0 ? -0.1 : 0.1;
    this.zoom = Math.max(0.3, Math.min(3, this.zoom + delta));
  }

  onMouseDown(event: MouseEvent): void {
    if ((event.target as HTMLElement).closest('button') || (event.target as HTMLElement).closest('.tree-node')) return;
    this.isPanning = true;
    this.startX = event.clientX - this.panX;
    this.startY = event.clientY - this.panY;
  }

  onMouseMove(event: MouseEvent): void {
    if (!this.isPanning) return;
    this.panX = event.clientX - this.startX;
    this.panY = event.clientY - this.startY;
  }

  onMouseUp(): void {
    this.isPanning = false;
  }

  isInPath(node: TreeNode): boolean {
    if (!this.selectedNode) return false;
    let current: TreeNode | null = this.selectedNode;
    while (current) {
      if (current.id === node.id) return true;
      current = current.parent;
    }
    return false;
  }

  getNodeStarCount(node: TreeNode): number {
    return node.likeCount;
  }

  getNodeSize(node: TreeNode): number {
    const base = 1;
    const likeBoost = Math.min(node.likeCount / 20, 0.3);
    const childBoost = Math.min(node.children.length * 0.1, 0.3);
    return base + likeBoost + childBoost;
  }

  parseContent(raw: string | undefined): BlockContentItem[] {
    if (!raw) return [];
    try { return JSON.parse(raw) as BlockContentItem[]; } catch { return []; }
  }

  editBlock(node: TreeNode): void {
    if (!node.fetched) return;
    const isOwner = this.currentUser?.id === node.ownerId;
    this.modalService.open(BlockEditorComponent, {
      title: isOwner ? 'blocks.editor.actions.edit' : 'blocks.editor.actions.fork',
      inputs: {
        mode: 'edit', blockId: node.id, blockName: node.name,
        blockDifficulty: node.difficulty, blockTags: node.tags,
        initialContent: this.parseContent(node.versions.length ? node.versions[node.versions.length - 1].content : undefined),
      },
      outputs: {
        save: (data: any) => {
          const obs = isOwner
            ? this.blockService.versionBlock(data as BlockVersionCreateRequestDTO)
            : this.blockService.forkBlock({ ...data, name: node.name, difficulty: node.difficulty, tags: node.tags, slug: '' } as BlockForkCreateRequestDTO);
          obs.subscribe({ next: () => { this.modalService.close(); this.loadTree(this.blockId); }, error: () => {} });
        },
      },
    });
  }

  openTreeFor(id: number): void {
    this.loadTree(id);
  }

  getVersionCount(node: TreeNode): number {
    return node.versions.length;
  }

  getLatestVersionDate(node: TreeNode): string {
    if (node.versions.length === 0) return '';
    return node.versions[node.versions.length - 1].createdDatetime;
  }

  trackByNodeId(_index: number, node: TreeNode): number { return node.id; }
  trackByVersionId(_index: number, v: BlockVersionResponseDTO): number { return v.id; }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
