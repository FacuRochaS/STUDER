import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BlockResponseDTO, BlockCompleteResponseDTO } from '../../block.model';
import { RouterModule } from '@angular/router';
import { RichTextComponent } from '../../../../shared/components/rich-text/rich-text.component';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'studer-block-header',
  standalone: true,
  imports: [CommonModule, RouterModule, RichTextComponent, TranslateModule],
  templateUrl: './block-header.component.html',
  styleUrls: ['./block-header.component.css']
})
export class BlockHeaderComponent {
  @Input() block: BlockResponseDTO | BlockCompleteResponseDTO | null = null;
  // Placeholder for like state
  isLiked = false;

  toggleLike(): void {
    this.isLiked = !this.isLiked;
    // Future: Call a service to update the like status
  }
}
