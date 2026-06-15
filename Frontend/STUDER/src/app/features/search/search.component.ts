import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { TranslatePipe } from '@ngx-translate/core';
import { UserPublic } from '../users/user.model';
import { UserService } from '../users/user.service';
import { RichTextComponent } from '../../shared/components/rich-text/rich-text.component';

@Component({
  selector: 'studer-search',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, TranslatePipe, RichTextComponent],
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.css']
})
export class SearchComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();

  query = '';
  results: UserPublic[] = [];
  loading = false;
  hasMore = false;
  page = 0;
  readonly pageSize = 20;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly userService: UserService
  ) {}

  ngOnInit(): void {
    this.route.queryParamMap
      .pipe(takeUntil(this.destroy$))
      .subscribe(params => {
        this.query = (params.get('q') ?? '').trim();
        this.page = 0;
        this.results = [];
        this.hasMore = false;
        if (this.query) {
          this.fetchResults();
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  submitSearch(): void {
    const trimmed = this.query.trim();
    if (!trimmed) {
      this.results = [];
      this.hasMore = false;
      return;
    }
    this.router.navigate(['/search'], { queryParams: { q: trimmed } });
  }

  loadMore(): void {
    if (this.loading || !this.hasMore) return;
    this.page += 1;
    this.fetchResults(true);
  }

  trackByUserId(_: number, user: UserPublic): number {
    return user.id;
  }

  getUserInitials(user: UserPublic): string {
    const first = user.firstName?.[0] ?? '';
    const last = user.lastName?.[0] ?? '';
    const fallback = user.username?.[0] ?? '';
    const initials = `${first}${last}`.trim();
    return initials || fallback.toUpperCase();
  }

  private fetchResults(append = false): void {
    this.loading = true;
    this.userService.searchUsers(this.query, this.page, this.pageSize).subscribe({
      next: response => {
        this.results = append ? [...this.results, ...response.users] : response.users;
        this.hasMore = response.hasMore;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }
}
