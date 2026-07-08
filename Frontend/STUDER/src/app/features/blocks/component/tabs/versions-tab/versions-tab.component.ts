import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BlockCompleteResponseDTO } from '../../../block.model';
import {TranslatePipe} from '@ngx-translate/core';

@Component({
  selector: 'studer-versions-tab',
  standalone: true,
  imports: [CommonModule, TranslatePipe],
  templateUrl: './versions-tab.component.html',
  styleUrls: ['./versions-tab.component.css']
})
export class VersionsTabComponent {
  @Input() block: BlockCompleteResponseDTO | null = null;
  selectedVersionId: number | null = null;

  ngOnChanges(): void {
    if (this.block && this.block.versions.length > 0) {
      this.selectedVersionId = this.block.versions[this.block.versions.length - 1].id;
    }
  }

  selectVersion(id: number): void {
    this.selectedVersionId = id;
    // Future: Load and display the content of the selected version
  }
}
