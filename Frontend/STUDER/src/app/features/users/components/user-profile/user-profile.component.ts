import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';
import { UserPublic, UserUpdateRequestDTO, User } from '../../user.model';
import { UserService } from '../../user.service';
import { FriendService } from '../../../friends/friend.service';
import { FriendStatusResponseDTO } from '../../../friends/friend.model';
import { AuthStateService } from '../../../../core/auth/auth-state.service';
import { RichTextComponent } from '../../../../shared/components/rich-text/rich-text.component';
import { UsernameComponent } from '../../../../shared/components/username/username.component';
import { BlockService } from '../../../blocks/block.service';
import { BlockResponseDTO, BlockCreateRequestDTO } from '../../../blocks/block.model';
import { ModalService } from '../../../../shared/services/modal.service';
import { BlockEditorComponent } from '../../../blocks/editor/block-editor.component';
import { BlockCardComponent } from '../../../blocks/component/block-card/block-card.component';
import { CourseService } from '../../../courses/course.service';
import { CourseResponseDTO } from '../../../courses/course.model';
import { FeedService } from '../../../feed/feed.service';
import { Router } from '@angular/router';

interface ProfileTab {
  id: string;
  label: string;
  icon: string;
}

@Component({
  selector: 'studer-user-profile',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    TranslateModule,
    RichTextComponent,
    UsernameComponent,
    BlockCardComponent,
  ],
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.css'],
})
export class UserProfileComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();
  private useCurrentUserRoute = false;

  user: UserPublic | null = null;
  loading = false;

  profileTabs: ProfileTab[] = [
    { id: 'feed', label: 'feed.title', icon: 'pi pi-send' },
    { id: 'blocks', label: 'search.tabs.blocks', icon: 'pi pi-cube' },
    { id: 'courses', label: 'search.tabs.courses', icon: 'pi pi-book' },
    { id: 'stats', label: 'blocks.tree.tab_stats', icon: 'pi pi-chart-bar' },
  ];
  activeTab = 'feed';

  blocks: BlockResponseDTO[] = [];
  blocksLoading = false;

  userCourses: CourseResponseDTO[] = [];
  coursesLoading = false;

  userPosts: any[] = [];
  postsLoading = false;
  followLoading = false;
  friendStatus: FriendStatusResponseDTO | null = null;
  currentUserId: number | null = null;
  currentUser: User | null = null;
  imagePreviewUrl: string | null = null;

  levelInfo = { level: 1, currentPoints: 0, nextLevelPoints: 20, progress: 0, ringDashOffset: 515.2 };

  constructor(
    private readonly route: ActivatedRoute,
    private readonly userService: UserService,
    private readonly friendService: FriendService,
    private readonly authState: AuthStateService,
    private readonly blockService: BlockService,
    private readonly modalService: ModalService,
    private readonly courseService: CourseService,
    private readonly feedService: FeedService,
    private readonly router: Router,
  ) {}

  setActiveTab(tabId: string): void {
    this.activeTab = tabId;
    if (tabId === 'feed' && this.user && this.userPosts.length === 0) this.loadUserPosts(this.user.id);
    if (tabId === 'courses' && this.user && this.userCourses.length === 0) this.loadUserCourses(this.user.username);
  }

  goToFeed(): void { this.router.navigate(['/feed']); }

  ngOnInit(): void {
    this.authState.user$.pipe(takeUntil(this.destroy$)).subscribe((user) => {
      this.currentUserId = user?.id ?? null;
      this.currentUser = user ?? null;
      if (this.useCurrentUserRoute && this.currentUser) {
        this.setCurrentUserProfile();
      }
    });

    this.route.paramMap.pipe(takeUntil(this.destroy$)).subscribe((params) => {
      const identifier = params.get('identifier') ?? '';
      if (!identifier || identifier.toLowerCase() === 'me') {
        this.useCurrentUserRoute = true;
        this.setCurrentUserProfile();
        return;
      }
      this.useCurrentUserRoute = false;
      this.loadUser(identifier);
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  get isOwnProfile(): boolean {
    return !!this.user && this.user.id === this.currentUserId;
  }

  get canShowSocialActions(): boolean {
    return !!this.user && this.currentUserId !== null && !this.isOwnProfile;
  }

  get avatarUrl(): string | null {
    return (
      this.imagePreviewUrl ||
      this.user?.profilePictureAvatarUrl ||
      this.user?.profilePictureThumbnailUrl ||
      null
    );
  }

  get ringDashOffset(): number {
    return this.levelInfo.ringDashOffset;
  }

  get initials(): string {
    if (!this.user) return '';
    const first = this.user.firstName?.[0] ?? '';
    const last = this.user.lastName?.[0] ?? '';
    const fallback = this.user.username?.[0] ?? '';
    const initials = `${first}${last}`.trim();
    return (initials || fallback).toUpperCase();
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    if (file) {
      this.imagePreviewUrl = URL.createObjectURL(file);
      this.uploadProfilePicture(file);
    }
  }

  private uploadProfilePicture(file: File): void {
    const request: UserUpdateRequestDTO = {
      email: "aaaaaaffafa@gmail.com",
      password: null
    };


    this.userService.updateMe(request, file).subscribe({
      next: () => {
        // Optionally refresh user data from auth state
      },
      error: () => {
        this.imagePreviewUrl = null; // Revert preview on error
      },
    });
  }

  openBlockEditor(): void {
    this.modalService.open(BlockEditorComponent, {
      title: 'Crear nuevo bloque',
      inputs: {
        mode: 'create',
      },
      outputs: {
        save: (request: BlockCreateRequestDTO) => {
          this.blockService.createBlock(request).subscribe({
            next: () => {
              this.modalService.close();
              if (this.user) {
                this.loadUserBlocks(this.user.id);
              }
            },
            error: () => {
              // Handle error
            },
          });
        },
      },
    });
  }

  onFollowToggle(): void {
    if (!this.user || this.followLoading || this.isOwnProfile) {
      return;
    }
    this.followLoading = true;

    const handleResponse = {
      next: () => {
        this.followLoading = false;
        if (this.user) {
          this.loadFriendStatus(this.user.id);
        }
      },
      error: () => {
        this.followLoading = false;
      },
    };

    if (this.friendStatus?.isFollowing) {
      this.friendService.unfollowUser(this.user.id).subscribe(handleResponse);
    } else {
      this.friendService.followUser(this.user.id).subscribe(handleResponse);
    }
  }

  private loadUser(identifier: string): void {
    this.loading = true;
    const isNumericId = /^\d+$/.test(identifier);
    const request = isNumericId
      ? this.userService.getById(Number(identifier))
      : this.userService.getByUsername(
          identifier.startsWith('@') ? identifier.slice(1) : identifier
        );


    request.subscribe({
      next: (user) => this.handleUserSuccess(user),
      error: () => this.handleUserError(),
    });
  }

  private handleUserSuccess(user: UserPublic): void {
    this.user = user;
    this.loading = false;
    this.levelInfo = this.getLevelInfo(user.points || 0);
    if (this.canShowSocialActions) {
      this.loadFriendStatus(user.id);
    }
    this.loadUserBlocks(user.id);
  }

  private handleUserError(): void {
    this.user = null;
    this.friendStatus = null;
    this.loading = false;
  }

  private setCurrentUserProfile(): void {
    if (!this.currentUser) {
      this.user = null;
      this.loading = true;
      return;
    }
    this.loading = true;
    this.userService.getById(this.currentUser.id).subscribe({
      next: (user) => {
        this.user = user;
        this.loading = false;
        this.levelInfo = this.getLevelInfo(user.points || 0);
        this.loadUserBlocks(user.id);
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  private loadFriendStatus(userId: number): void {
    this.friendService.getFriendStatus(userId).subscribe({
      next: (status) => (this.friendStatus = status),
      error: () => (this.friendStatus = null),
    });
  }

  private getLevelInfo(points: number): { level: number; currentPoints: number; nextLevelPoints: number; progress: number; ringDashOffset: number } {
    const thresholds = this.buildThresholds(20);
    let level = 1;
    for (let i = thresholds.length - 1; i >= 0; i--) {
      if (points >= thresholds[i]) { level = i + 1; break; }
    }
    const currentThreshold = thresholds[level - 1];
    const nextThreshold = thresholds[level] || (thresholds[level - 1] + (thresholds[level - 1] - thresholds[level - 3]));
    const progress = ((points - currentThreshold) / (nextThreshold - currentThreshold)) * 100;
    const circumference = 515.2;
    const ringDashOffset = circumference - (circumference * Math.min(progress, 100) / 100);
    return { level, currentPoints: points - currentThreshold, nextLevelPoints: nextThreshold - currentThreshold, progress: Math.min(progress, 100), ringDashOffset };
  }

  private buildThresholds(count: number): number[] {
    const base = [0, 10, 20, 40, 80, 100, 200, 300, 500, 700, 1000, 2000, 4000];
    while (base.length < count) {
      const len = base.length;
      base.push(base[len - 1] + (base[len - 1] - base[len - 3]));
    }
    return base;
  }

  private loadUserBlocks(userId: number): void {
    this.blocksLoading = true;
    this.blockService.getBlockByUser(userId, 0).subscribe({
      next: (page) => { this.blocks = page.blocks; this.blocksLoading = false; },
      error: () => { this.blocks = []; this.blocksLoading = false; },
    });
  }

  private loadUserCourses(username: string): void {
    this.coursesLoading = true;
    this.courseService.getCoursesByUsername(username, 0).pipe(takeUntil(this.destroy$)).subscribe({
      next: (page) => { this.userCourses = page.courses; this.coursesLoading = false; },
      error: () => { this.userCourses = []; this.coursesLoading = false; },
    });
  }

  private loadUserPosts(userId: number): void {
    this.postsLoading = true;
    this.feedService.getUserPosts(userId, 0).pipe(takeUntil(this.destroy$)).subscribe({
      next: (page) => { this.userPosts = page.posts; this.postsLoading = false; },
      error: () => { this.userPosts = []; this.postsLoading = false; },
    });
  }
}
