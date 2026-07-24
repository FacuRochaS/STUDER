import { Component, Input, OnChanges, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { BlockService } from '../../../block.service';
import { BlockStatsDTO } from '../../../block.model';

@Component({
  selector: 'studer-stats-tab',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './stats-tab.component.html',
  styleUrls: ['./stats-tab.component.css']
})
export class StatsTabComponent implements OnChanges {
  @Input() blockId!: number;
  private blockService = inject(BlockService);

  stats: BlockStatsDTO | null = null;
  loading = false;

  ngOnChanges(): void {
    if (this.blockId) {
      this.loading = true;
      this.blockService.getBlockStats(this.blockId).subscribe({
        next: (s) => { this.stats = s; this.loading = false; },
        error: () => { this.loading = false; },
      });
    }
  }
}
