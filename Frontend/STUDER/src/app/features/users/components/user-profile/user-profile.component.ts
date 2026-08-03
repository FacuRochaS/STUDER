import { Component, OnDestroy, OnInit, ElementRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { Subject, takeUntil, firstValueFrom } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';
import { UserPublic, UserUpdateRequestDTO, User } from '../../user.model';
import { UserService } from '../../user.service';
import { FriendService } from '../../../friends/friend.service';
import { FriendStatusResponseDTO } from '../../../friends/friend.model';
import { AuthStateService } from '../../../../core/auth/auth-state.service';
import { UsernameComponent } from '../../../../shared/components/username/username.component';
import { BlockCardComponent } from '../../../blocks/component/block-card/block-card.component';
import { BlockResponseDTO } from '../../../blocks/block.model';
import { CourseResponseDTO } from '../../../courses/course.model';
import { CourseService } from '../../../courses/course.service';
import { FeedService } from '../../../feed/feed.service';
import { DiscussionResponseDTO } from '../../../discussions/discussion.model';
import { DiscussionService } from '../../../discussions/discussion.service';
import { BlockService } from '../../../blocks/block.service';
import { PostCardComponent } from '../../../home/post-card/post-card.component';
import { AutoAnimateDirective } from '../../../../shared/directives/auto-animate.directive';
import { LoaderComponent } from '../../../../shared/components/loader/loader.component';

interface ActivityItem {
  type: 'post' | 'block' | 'course' | 'discussion';
  data: any;
  date: string;
}

@Component({
  selector: 'studer-user-profile',
  standalone: true,
  imports: [
    CommonModule, RouterModule, TranslateModule, FormsModule,
    UsernameComponent, BlockCardComponent, PostCardComponent,
    AutoAnimateDirective, LoaderComponent,
  ],
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.css'],
})
export class UserProfileComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();
  private useCurrentUserRoute = false;

  user: UserPublic | null = null;
  loading = false;

  blocks: BlockResponseDTO[] = [];
  userCourses: CourseResponseDTO[] = [];
  userPosts: any[] = [];
  userDiscussions: DiscussionResponseDTO[] = [];
  loadingMore = false;

  followLoading = false;
  friendStatus: FriendStatusResponseDTO | null = null;
  currentUserId: number | null = null;
  currentUser: User | null = null;
  imagePreviewUrl: string | null = null;
  showEditModal = false;
  editPassword = '';
  editPasswordConfirm = '';
  errorMsg = '';
  saving = false;
  selectedFile: File | null = null;

  activityFilter = 'all';
  filters = [
    { id: 'all', label: 'profile.filters.all', icon: '' },
    { id: 'posts', label: 'profile.filters.posts', icon: '' },
    { id: 'blocks', label: 'profile.filters.blocks', icon: '' },
    { id: 'courses', label: 'profile.filters.courses', icon: '' },
    { id: 'discussions', label: 'profile.filters.discussions', icon: '' },
  ];

  levelInfo = { level: 1, currentPoints: 0, nextLevelPoints: 20, progress: 0, ringDashOffset: 515.2 };

  private readonly route = inject(ActivatedRoute);
  private readonly userService = inject(UserService);
  private readonly friendService = inject(FriendService);
  private readonly authState = inject(AuthStateService);
  private readonly courseService = inject(CourseService);
  private readonly feedService = inject(FeedService);
  private readonly discussionService = inject(DiscussionService);
  private readonly blockService = inject(BlockService);
  private readonly router = inject(Router);

  get isOwnProfile(): boolean { return !!this.user && this.user.id === this.currentUserId; }
  get avatarUrl(): string | null { return this.imagePreviewUrl || this.user?.profilePictureAvatarUrl || this.user?.profilePictureThumbnailUrl || null; }
  get ringDashOffset(): number { return this.levelInfo.ringDashOffset; }
  get initials(): string {
    if (!this.user) return '';
    return ((this.user.firstName?.[0] ?? '') + (this.user.lastName?.[0] ?? '')).toUpperCase() || (this.user.username?.[0] ?? '').toUpperCase();
  }

  get filteredActivity(): ActivityItem[] {
    const all: ActivityItem[] = [
      ...this.userPosts.map(p => ({ type: 'post' as const, data: p, date: p.createdDatetime })),
      ...this.blocks.map(b => ({ type: 'block' as const, data: b, date: b.createdDatetime })),
      ...this.userCourses.map(c => ({ type: 'course' as const, data: c, date: c.createdDatetime })),
      ...this.userDiscussions.map(d => ({ type: 'discussion' as const, data: d, date: d.createdAt })),
    ].sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());

    if (this.activityFilter === 'all') return all;
    return all.filter(a => a.type + 's' as string === this.activityFilter || a.type + 'es' as string === this.activityFilter || a.type === this.activityFilter);
  }

  async ngOnInit(): Promise<void> {
    const user = await firstValueFrom(this.authState.user$.pipe(takeUntil(this.destroy$)));
    this.currentUserId = user?.id ?? null;
    this.currentUser = user ?? null;

    this.route.paramMap.pipe(takeUntil(this.destroy$)).subscribe(params => {
      const id = params.get('identifier') ?? '';
      if (!id || id.toLowerCase() === 'me') {
        this.useCurrentUserRoute = true;
        this.setCurrentUserProfile();
        return;
      }
      this.useCurrentUserRoute = false;
      this.loadUser(id);
    });
  }

  ngOnDestroy(): void { this.destroy$.next(); this.destroy$.complete(); }

  onFileSelected(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    this.selectedFile = file;
    this.imagePreviewUrl = URL.createObjectURL(file);
  }

  saveProfile(): void {
    this.errorMsg = '';
    if (this.editPassword && this.editPassword !== this.editPasswordConfirm) {
      this.errorMsg = 'profile.password_mismatch';
      return;
    }
    this.saving = true;
    const request: UserUpdateRequestDTO = {
      email: 'user@studer.com',
      password: this.editPassword || null,
    };
    this.userService.updateMe(request, this.selectedFile ?? undefined).subscribe({
      next: () => {
        this.saving = false;
        this.showEditModal = false;
        this.editPassword = '';
        this.editPasswordConfirm = '';
        this.selectedFile = null;
      },
      error: () => {
        this.saving = false;
        this.errorMsg = 'profile.save_error';
      },
    });
  }

  onFollowToggle(): void {
    if (!this.user || this.followLoading || this.isOwnProfile) return;
    this.followLoading = true;
    if (this.friendStatus?.isFollowing) {
      this.friendService.unfollowUser(this.user.id).subscribe({
        next: () => { this.followLoading = false; this.loadFriendStatus(this.user!.id); },
        error: () => this.followLoading = false
      });
    } else {
      this.friendService.followUser(this.user.id).subscribe({
        next: () => { this.followLoading = false; this.loadFriendStatus(this.user!.id); },
        error: () => this.followLoading = false
      });
    }
  }

  private loadUser(id: string): void {
    this.loading = true;
    const req = /^\d+$/.test(id)
      ? this.userService.getById(Number(id))
      : this.userService.getByUsername(id.startsWith('@') ? id.slice(1) : id);
    req.subscribe({ next: u => this.onUserLoaded(u), error: () => { this.user = null; this.loading = false; } });
  }

  private setCurrentUserProfile(): void {
    if (!this.currentUser) { this.user = null; this.loading = true; return; }
    this.loading = true;
    this.userService.getById(this.currentUser.id).subscribe({
      next: u => this.onUserLoaded(u),
      error: () => this.loading = false,
    });
  }

  private onUserLoaded(u: UserPublic): void {
    this.user = u;
    this.loading = false;
    this.levelInfo = this.getLevelInfo(u.points || 0);
    if (this.currentUserId && u.id !== this.currentUserId) this.loadFriendStatus(u.id);
    this.loadAllActivity(u.id, u.username);
  }

  private loadAllActivity(userId: number, username: string): void {
    this.feedService.getUserPosts(userId, 0).subscribe(p => this.userPosts = p.posts);
    this.courseService.getCoursesByUsername(username, 0).subscribe(c => this.userCourses = c.courses);
    this.discussionService.getUserDiscussions(username, 0).subscribe(d => this.userDiscussions = d.discussions);
    this.blockService.getBlockByUser(userId, 0).subscribe(b => this.blocks = b.blocks);
  }

  private loadFriendStatus(userId: number): void {
    this.friendService.getFriendStatus(userId).subscribe({ next: s => this.friendStatus = s, error: () => this.friendStatus = null });
  }

  private getLevelInfo(points: number) {
    const base = [0, 10, 20, 40, 80, 100, 200, 300, 500, 700, 1000, 2000, 4000];
    let level = 1;
    for (let i = base.length - 1; i >= 0; i--) { if (points >= base[i]) { level = i + 1; break; } }
    const curr = base[level - 1];
    const next = base[level] || (base[level - 1] + (base[level - 1] - base[level - 3]));
    const progress = ((points - curr) / (next - curr)) * 100;
    const circ = 515.2;
    return { level, currentPoints: points - curr, nextLevelPoints: next - curr, progress: Math.min(progress, 100), ringDashOffset: circ - (circ * Math.min(progress, 100) / 100) };
  }
}
