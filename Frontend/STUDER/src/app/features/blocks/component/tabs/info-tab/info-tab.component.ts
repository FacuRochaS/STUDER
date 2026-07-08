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

  get versionNumber(): number | undefined {
    if (!this.block) return undefined;
    if ('version' in this.block) {
      return this.block.version.versionNumber;
    }
    if (this.block.versions && this.block.versions.length > 0) {
      return this.block.versions[this.block.versions.length - 1].versionNumber;
    }
    return undefined;
  }

  get tagsAsText(): string {
    return this.block?.tags.map(t => `#${t}`).join(' ') || '';
  }

  get authorAsText(): string {
    return this.block ? `@${this.block.owner.username}` : '';
  }
}
