import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BlockResponseDTO, BlockCompleteResponseDTO } from '../../../block.model';
import { TranslateModule } from '@ngx-translate/core';
import { RichTextComponent } from '../../../../../shared/components/rich-text/rich-text.component';

@Component({
  selector: 'studer-info-tab',
  standalone: true,
  imports: [CommonModule, TranslateModule, RichTextComponent],
  templateUrl: './info-tab.component.html',
  styleUrls: ['./info-tab.component.css']
})
export class InfoTabComponent {
  @Input() block: BlockResponseDTO | BlockCompleteResponseDTO | null = null;

  get versionNumber(): string {
    if (!this.block) return '?';
    if ('version' in this.block && this.block.version) {
      return String(this.block.version.versionNumber);
    }
    if ('versions' in this.block && (this.block as BlockCompleteResponseDTO).versions?.length) {
      return String((this.block as BlockCompleteResponseDTO).versions.slice(-1)[0].versionNumber);
    }
    return 'Latest';
  }

  get tagsAsText(): string {
    return this.block?.tags.map(t => `#${t}`).join(' ') || '';
  }

  get authorAsText(): string {
    return this.block ? `@${this.block.owner.username}` : '';
  }

  get forkCount(): number {
    if (!this.block) return 0;
    if ('forkCount' in this.block) return (this.block as any).forkCount ?? 0;
    return 0;
  }

  get likeCount(): number {
    if (!this.block) return 0;
    if ('likeCount' in this.block) return this.block.likeCount ?? 0;
    if ('likesCount' in this.block) return (this.block as any).likesCount ?? 0;
    return 0;
  }
}
