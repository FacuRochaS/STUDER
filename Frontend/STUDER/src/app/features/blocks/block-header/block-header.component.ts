import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BlockResponseDTO } from '../block.model';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'studer-block-header',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './block-header.component.html',
  styleUrls: ['./block-header.component.css']
})
export class BlockHeaderComponent {
  @Input() block: BlockResponseDTO | null = null;
  // Placeholder for like state
  isLiked = false;

  toggleLike(): void {
    this.isLiked = !this.isLiked;
    // Future: Call a service to update the like status
  }
}
