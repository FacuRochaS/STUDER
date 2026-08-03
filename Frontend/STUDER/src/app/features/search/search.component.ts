import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';
import { SearchService, SearchCategory } from './search.service';
import { UserPublic } from '../users/user.model';
import { BlockResponseDTO } from '../blocks/block.model';
import { CourseResponseDTO } from '../courses/course.model';
import { ContestResponseDTO } from '../contest/contest.model';

@Component({
  selector: 'studer-search',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule, RouterModule],
  template: `
    <div class="search-page">
      <div class="search-page__header">
        <h1 class="search-page__title">{{ 'search.title' | translate }}</h1>
        <div class="search-page__input">
          <span class="pi pi-search"></span>
          <input type="text" [(ngModel)]="query" (keydown.enter)="makeSearch()" [placeholder]="'search.placeholder' | translate" />
          <button type="button" (click)="makeSearch()">{{ 'search.action' | translate }}</button>
        </div>
      </div>

      <div class="search-page__tabs">
        <button class="search-tab" [class.active]="activeTab === 'users'" (click)="selectTab('users')">
          <i class="pi pi-users"></i><span>{{ 'search.tabs.users' | translate }}</span>
        </button>
        <button class="search-tab" [class.active]="activeTab === 'blocks'" (click)="selectTab('blocks')">
          <i class="pi pi-cube"></i><span>{{ 'search.tabs.blocks' | translate }}</span>
        </button>
        <button class="search-tab" [class.active]="activeTab === 'courses'" (click)="selectTab('courses')">
          <i class="pi pi-book"></i><span>{{ 'search.tabs.courses' | translate }}</span>
        </button>
        <button class="search-tab" [class.active]="activeTab === 'contests'" (click)="selectTab('contests')">
          <i class="pi pi-list-check"></i><span>{{ 'search.tabs.contests' | translate }}</span>
        </button>
      </div>

      <div class="search-page__results">
        <div *ngIf="loading" class="search-page__loading">{{ 'system.loading' | translate }}</div>

        <ng-container *ngIf="!loading && activeTab === 'users'">
          <div *ngIf="users.length === 0" class="search-page__empty">{{ 'search.empty' | translate }}</div>
          <div class="search-page__grid">
            <a *ngFor="let user of users; trackBy: trackById" class="search-card" [routerLink]="['/user', '@' + user.username]">
              <div class="search-card__avatar">
                <img *ngIf="user.profilePictureThumbnailUrl" [src]="user.profilePictureThumbnailUrl" [alt]="user.username" />
                <span *ngIf="!user.profilePictureThumbnailUrl">{{ (user.firstName[0] || '') + (user.lastName[0] || '') || user.username[0].toUpperCase() }}</span>
              </div>
              <div class="search-card__info">
                <div class="search-card__name">{{ user.firstName }} {{ user.lastName }}</div>
                <div class="search-card__username">{{ '@' + user.username }}</div>
              </div>
            </a>
          </div>
        </ng-container>

        <ng-container *ngIf="!loading && activeTab === 'blocks'">
          <div *ngIf="blocks.length === 0" class="search-page__empty">{{ 'search.empty' | translate }}</div>
          <div class="search-page__grid">
            <div *ngFor="let block of blocks; trackBy: trackById" class="search-card">
              <div class="search-card__icon cube"><i class="pi pi-cube"></i></div>
              <div class="search-card__info">
                <div class="search-card__name">{{ block.name }}</div>
                <div class="search-card__meta">
                  <span [class]="'badge badge--' + (block.difficulty || 'normal' | lowercase)">{{ block.difficulty }}</span>
                  <span>{{ block.owner?.firstName || '' }} {{ block.owner?.lastName || '' }}</span>
                </div>
              </div>
            </div>
          </div>
        </ng-container>

        <ng-container *ngIf="!loading && activeTab === 'courses'">
          <div *ngIf="courses.length === 0" class="search-page__empty">{{ 'search.empty' | translate }}</div>
          <div class="search-page__grid">
            <a *ngFor="let course of courses; trackBy: trackById" class="search-card" [routerLink]="['/courses', course.id]">
              <div class="search-card__icon book"><i class="pi pi-book"></i></div>
              <div class="search-card__info">
                <div class="search-card__name">{{ course.name }}</div>
                <div class="search-card__meta">
                  <span>{{ course.owner?.firstName || '' }} {{ course.owner?.lastName || '' }}</span>
                  <span>{{ (course.blocks?.length || 0) }} blocks</span>
                </div>
              </div>
            </a>
          </div>
        </ng-container>

        <ng-container *ngIf="!loading && activeTab === 'contests'">
          <div *ngIf="contests.length === 0" class="search-page__empty">{{ 'search.empty' | translate }}</div>
          <div class="search-page__grid">
            <a *ngFor="let contest of contests; trackBy: trackById" class="search-card" [routerLink]="['/contest', contest.id]">
              <div class="search-card__icon contest"><i class="pi pi-list-check"></i></div>
              <div class="search-card__info">
                <div class="search-card__name">{{ contest.title }}</div>
                <div class="search-card__meta">
                  <span>{{ contest.status }}</span>
                  <span>{{ contest.participantCount }}</span>
                </div>
              </div>
            </a>
          </div>
        </ng-container>
      </div>
    </div>
  `,
  styles: [`
    .search-page { display: flex; flex-direction: column; gap: 1.25rem; padding: 1.5rem; }
    .search-page__header { display: flex; flex-direction: column; gap: 0.75rem; }
    .search-page__title { margin: 0; color: var(--color-text-prim); font-size: 1.4rem; }
    .search-page__input { display: flex; align-items: center; gap: 0.5rem; background: var(--color-bg); border: 1px solid var(--border); border-radius: 0.4rem; padding: 0.5rem 0.75rem; }
    .search-page__input input { flex: 1; border: none; outline: none; background: transparent; color: var(--color-text-prim); font-size: 0.95rem; }
    .search-page__input button { border: none; background: var(--color-primary); color: var(--color-text-btn); padding: 0.45rem 0.9rem; border-radius: 0.3rem; cursor: pointer; }
    .search-page__tabs { display: flex; gap: 0.25rem; border-bottom: 1px solid var(--border); }
    .search-tab { display: flex; align-items: center; gap: 0.4rem; padding: 0.5rem 0.85rem; border: none; background: transparent; color: var(--color-text-secu); font-size: 0.85rem; cursor: pointer; border-bottom: 2px solid transparent; font-family: inherit; }
    .search-tab:hover { color: var(--color-text-prim); }
    .search-tab.active { color: var(--color-primary); border-bottom-color: var(--color-primary); font-weight: 600; }
    .search-page__results { display: flex; flex-direction: column; gap: 1rem; }
    .search-page__grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 0.75rem; }
    .search-page__loading, .search-page__empty { text-align: center; color: var(--color-text-secu); padding: 2rem; }
    .search-card { display: flex; align-items: center; gap: 0.75rem; padding: 0.75rem; box-shadow: 0 0 0 1px var(--border); background: var(--color-bg); border-radius: 0.35rem; text-decoration: none; color: inherit; transition: box-shadow 0.2s; }
    .search-card:hover { box-shadow: 0 0 0 1px var(--color-primary), 0 6px 16px var(--box-shadow-color); }
    .search-card__avatar { width: 42px; height: 42px; border-radius: 50%; background: var(--sidebar-hover-bg); color: var(--color-text-prim); display: flex; align-items: center; justify-content: center; font-weight: 600; overflow: hidden; flex-shrink: 0; }
    .search-card__avatar img { width: 100%; height: 100%; object-fit: cover; }
    .search-card__icon { width: 42px; height: 42px; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-size: 1.2rem; flex-shrink: 0; }
    .search-card__icon.cube { background: rgba(66,133,244,0.1); color: var(--color-primary); }
    .search-card__icon.book { background: rgba(0,191,99,0.1); color: var(--color-correct); }
    .search-card__icon.contest { background: rgba(156,39,176,0.1); color: #9c27b0; }
    .search-card__info { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 0.2rem; }
    .search-card__name { font-weight: 600; color: var(--color-text-prim); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .search-card__username { font-size: 0.85rem; color: var(--color-text-secu); }
    .search-card__meta { font-size: 0.78rem; color: var(--color-text-secu); display: flex; gap: 0.5rem; align-items: center; }
    .badge { padding: 0.1rem 0.35rem; border-radius: 3px; font-size: 0.65rem; font-weight: 700; text-transform: uppercase; }
    .badge--easy { background: rgba(0,191,99,0.12); color: var(--color-correct); }
    .badge--normal { background: rgba(66,133,244,0.12); color: var(--color-primary); }
    .badge--hard { background: rgba(244,67,54,0.12); color: var(--color-error); }
    .badge--expert { background: rgba(255,117,31,0.12); color: var(--color-restriction); }
  `]
})
export class SearchComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();
  query = '';
  activeTab: SearchCategory = 'users';
  loading = false;
  users: UserPublic[] = [];
  blocks: BlockResponseDTO[] = [];
  courses: CourseResponseDTO[] = [];
  contests: ContestResponseDTO[] = [];

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly searchService: SearchService
  ) {}

  ngOnInit(): void {
    this.route.queryParamMap.pipe(takeUntil(this.destroy$)).subscribe(params => {
      this.query = (params.get('q') ?? '').trim();
      const cat = params.get('cat') as SearchCategory;
      if (cat) this.activeTab = cat;
      this.clearAll();
      if (this.query) this.fetchResults();
    });
  }

  ngOnDestroy(): void { this.destroy$.next(); this.destroy$.complete(); }

  makeSearch(): void { if (this.query.trim()) this.fetchResults(); }
  selectTab(id: SearchCategory): void { this.activeTab = id; this.clearAll(); if (this.query) this.fetchResults(); }
  trackById(_: number, item: any): number { return item.id; }

  private clearAll(): void { this.users = []; this.blocks = []; this.courses = []; this.contests = []; }

  private fetchResults(): void {
    this.loading = true;
    this.searchService.searchQuick(this.query).pipe(takeUntil(this.destroy$)).subscribe({
      next: (r) => { this.users = r.users || []; this.blocks = r.blocks || []; this.courses = r.courses || []; this.contests = r.contests || []; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }
}
