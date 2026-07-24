import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { Subject, takeUntil, retry } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { API_CONFIG } from '../../config/api.config';
import { AuthStateService } from '../../core/auth/auth-state.service';
import { ContestService } from '../contest/contest.service';
import { ContestResponseDTO } from '../contest/contest.model';
import { LoaderComponent } from '../../shared/components/loader/loader.component';
import { AdminDashboardComponent } from './components/dashboard/admin-dashboard.component';
import { AdminUsersComponent } from './components/users/admin-users.component';
import { AdminBlocksComponent } from './components/blocks/admin-blocks.component';
import { AdminCoursesComponent } from './components/courses/admin-courses.component';
import { AdminFeedComponent } from './components/feed/admin-feed.component';
import { AdminDiscussionsComponent } from './components/discussions/admin-discussions.component';
import { AdminContestsComponent } from './components/contests/admin-contests.component';
import { AdminTagsComponent } from './components/tags/admin-tags.component';

interface Tab { id: string; label: string; icon: string; }

@Component({
  selector: 'studer-admin',
  standalone: true,
  imports: [CommonModule, RouterModule, TranslateModule, LoaderComponent,
    AdminDashboardComponent, AdminUsersComponent, AdminBlocksComponent,
    AdminCoursesComponent, AdminFeedComponent, AdminDiscussionsComponent,
    AdminContestsComponent, AdminTagsComponent],
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.css']
})
export class AdminComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
  private auth = inject(AuthStateService);
  private cs = inject(ContestService);
  private http = inject(HttpClient);
  isAdmin = false; loading = true; d: any = {}; userData: any = {}; blockData: any = {}; courseData: any = {}; feedData: any = {}; discData: any = {}; contestData: any = {}; tagData: any = {}; contests: ContestResponseDTO[] = [];
  activeTab = 'dashboard';

  tabs: Tab[] = [
    { id: 'dashboard', label: 'admin.dashboard', icon: 'pi pi-chart-bar' },
    { id: 'users', label: 'admin.users', icon: 'pi pi-users' },
    { id: 'blocks', label: 'admin.blocks', icon: 'pi pi-cube' },
    { id: 'courses', label: 'admin.courses', icon: 'pi pi-book' },
    { id: 'feed', label: 'admin.feed', icon: 'pi pi-send' },
    { id: 'discussions', label: 'admin.discussions', icon: 'pi pi-comments' },
    { id: 'contests', label: 'admin.contests', icon: 'pi pi-list-check' },
    { id: 'tags', label: 'admin.tags', icon: 'pi pi-tag' },
  ];

  ngOnInit(): void { this.auth.user$.pipe(takeUntil(this.destroy$)).subscribe(u => { this.isAdmin = u?.role === 'ADMIN'; if (this.isAdmin) this.load(); else this.loading = false; }); }
  ngOnDestroy(): void { this.destroy$.next(); this.destroy$.complete(); }

  switchTab(tab: string): void { this.activeTab = tab; if (tab === 'users' && !this.userData?.cards) this.loadUsers(); if (tab === 'blocks' && !this.blockData?.cards) this.loadBlocks(); if (tab === 'courses' && !this.courseData?.cards) this.loadCourses(); if (tab === 'feed' && !this.feedData?.cards) this.loadFeed(); if (tab === 'discussions' && !this.discData?.cards) this.loadDiscussions(); if (tab === 'contests' && !this.contestData?.cards) this.loadContests(); if (tab === 'tags' && !this.tagData?.cards) this.loadTags(); }

  load(): void {
    this.loading = true;
    this.get('/admin/dashboard').subscribe({
      next: (m: any) => {
        const cards = m.cards || {};
        this.d = { ...cards, charts: m.charts,
          activeWeek: cards.activeUsersLast7Days, activeMonth: cards.activeUsersLast30Days,
          newUsersWeek: cards.newUsersToday,
          totalInactive: (cards.totalUsers || 0) - (cards.activeUsersLast30Days || 0)
        };
        this.loading = false;
      },
      error: () => this.loading = false
    });
    this.cs.list().pipe(takeUntil(this.destroy$)).subscribe((p: any) => this.contests = p.content || []);
  }

  loadUsers(): void { this.get('/admin/dashboard/users').subscribe((m: any) => this.userData = m); }
  loadBlocks(): void { this.get('/admin/dashboard/blocks').subscribe((m: any) => this.blockData = m); }
  loadCourses(): void { this.get('/admin/dashboard/courses').subscribe((m: any) => this.courseData = m); }
  loadFeed(): void { this.get('/admin/dashboard/feed').subscribe((m: any) => this.feedData = m); }
  loadDiscussions(): void { this.get('/admin/dashboard/discussions').subscribe((m: any) => this.discData = m); }
  loadContests(): void { this.get('/admin/dashboard/contests').subscribe((m: any) => this.contestData = m); }
  loadTags(): void { this.get('/admin/dashboard/tags').subscribe((m: any) => this.tagData = m); }

  private get(path: string) { return this.http.get(`${API_CONFIG.baseUrl}${path}`).pipe(retry(2), takeUntil(this.destroy$)); }

  finishContest(c: ContestResponseDTO): void { if (confirm(`Finish "${c.title}"?`)) this.cs.finishContest(c.id).pipe(takeUntil(this.destroy$)).subscribe(() => this.load()); }
}
