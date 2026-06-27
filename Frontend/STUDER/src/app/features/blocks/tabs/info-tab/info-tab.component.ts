import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BlockResponseDTO } from '../../block.model';

@Component({
  selector: 'studer-info-tab',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './info-tab.component.html',
  styleUrls: ['./info-tab.component.css']
})
export class InfoTabComponent {
  @Input() block: BlockResponseDTO | null = null;
}
