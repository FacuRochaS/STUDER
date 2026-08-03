import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { ContestService } from '../../contest.service';
import { ContestResponseDTO } from '../../contest.model';
import { LoaderComponent } from '../../../../shared/components/loader/loader.component';
import { AutoAnimateDirective } from '../../../../shared/directives/auto-animate.directive';

@Component({
  selector: 'studer-contest-list',
  standalone: true,
  imports: [CommonModule, TranslateModule, RouterLink, LoaderComponent, AutoAnimateDirective],
  templateUrl: 'contest-list.component.html',
  styleUrls: ['contest-list.component.css']
})
export class ContestListComponent implements OnInit {
  private contestService = inject(ContestService);
  loading = true;
  allContests: ContestResponseDTO[] = [];
  activeTab = 'preparation';

  tabs = [
    {id: 'preparation', label: 'contest.list.preparation', icon: 'pi pi-clock'},
    {id: 'active', label: 'contest.list.active_tab', icon: 'pi pi-play'},
    {id: 'finished', label: 'contest.list.finished', icon: 'pi pi-check-circle'},
  ];

  get filteredContests(): ContestResponseDTO[] {
    switch (this.activeTab) {
      case 'preparation':
        return this.allContests.filter(c => c.status === 'PREPARATION' || c.status === 'ANNOUNCED');
      case 'active':
        return this.allContests.filter(c => c.status === 'VALIDATION');
      case 'finished':
        return this.allContests.filter(c => c.status === 'RESULTS' || c.status === 'CANCELLED');
      default:
        return [];
    }
  }

  ngOnInit(): void {
    this.contestService.list().subscribe({
      next: (page) => {
        this.allContests = page.content as ContestResponseDTO[];
        this.loading = false;
      },
      error: () => this.loading = false,
    });
  }

  getStatusLabel(status: string): string {
    const map: Record<string, string> = {
      ANNOUNCED: 'contest.status.announced',
      PREPARATION: 'contest.status.preparation',
      VALIDATION: 'contest.status.validation',
      RESULTS: 'contest.status.results',
      CANCELLED: 'contest.status.cancelled',
    };
    return map[status] || status;
  }

  getMinLevel(pts: number): number {
    const lvl = [0, 10, 20, 40, 80, 100, 200, 300, 500, 700, 1000, 2000, 4000, 8000, 16000];
    for (let i = lvl.length - 1; i >= 0; i--) {
      if (pts >= lvl[i]) return i + 1;
    }
    return 1;
  }

}
