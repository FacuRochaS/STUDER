import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { TranslatePipe } from '@ngx-translate/core';
import { User, UserPublic } from '../../../../models/user.model';
import { UserService } from '../../../../services/user.service';
import { FriendService } from '../../../../services/friend.service';
import { FriendStatusResponseDTO } from '../../../../models/friend.model';
import { AuthStateService } from '../../../../core/auth/auth-state.service';
import { RichTextComponent } from '../../../../shared/components/rich-text/rich-text.component';

@Component({
  selector: 'studer-user-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslatePipe, RichTextComponent],
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.css']
})
export class UserProfileComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();
  private useCurrentUserRoute = false;

  user: UserPublic | null = null;
  loading = false;
  followLoading = false;
  saving = false;
  friendStatus: FriendStatusResponseDTO | null = null;
  currentUserId: number | null = null;
  currentUser: User | null = null;
  editEmail = '';
  editPassword = '';
  editPasswordConfirm = '';
  selectedFile: File | null = null;
  imagePreviewUrl: string | null = null;
  saveError = '';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly userService: UserService,
    private readonly friendService: FriendService,
    private readonly authState: AuthStateService
  ) {}

  ngOnInit(): void {
    this.authState.user$
      .pipe(takeUntil(this.destroy$))
      .subscribe(user => {
        this.currentUserId = user?.id ?? null;
        this.currentUser = user ?? null;
        if (this.useCurrentUserRoute && this.currentUser) {
          this.setCurrentUserProfile();
          return;
        }
        if (this.isOwnProfile && this.currentUser) {
          this.editEmail = this.currentUser.email ?? '';
        }
      });

    this.route.paramMap
      .pipe(takeUntil(this.destroy$))
      .subscribe(params => {
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
    return this.imagePreviewUrl || this.user?.profilePictureAvatarUrl || this.user?.profilePictureThumbnailUrl || null;
  }

  get initials(): string {
    if (!this.user) return '';
    const first = this.user.firstName?.[0] ?? '';
    const last = this.user.lastName?.[0] ?? '';
    const fallback = this.user.username?.[0] ?? '';
    const initials = `${first}${last}`.trim();
    return (initials || fallback).toUpperCase();
  }

  onMessage(): void {
    if (!this.user) return;
    this.router.navigate(['/messages'], { queryParams: { userId: this.user.id, username: this.user.username } });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    this.selectedFile = file;
    this.imagePreviewUrl = file ? URL.createObjectURL(file) : null;
  }

  saveProfile(): void {
    if (!this.user || this.saving) return;
    if (this.editPassword && this.editPassword !== this.editPasswordConfirm) {
      this.saveError = 'profile.password_mismatch';
      return;
    }

    this.saveError = '';
    this.saving = true;
    const request = {
      email: this.editEmail || null,
      password: this.editPassword || null
    };

    this.userService.updateMe(request, this.selectedFile).subscribe({
      next: () => {
        this.saving = false;
        this.editPassword = '';
        this.editPasswordConfirm = '';
        this.selectedFile = null;
        this.imagePreviewUrl = null;
        this.loadUser(this.user!.username);
      },
      error: () => {
        this.saving = false;
        this.saveError = 'profile.save_error';
      }
    });
  }

  onFollowToggle(): void {
    if (!this.user || this.followLoading || this.isOwnProfile) return;
    this.followLoading = true;

    if (this.friendStatus?.isFollowing) {
      this.friendService.unfollowUser(this.user.id).subscribe({
        next: () => {
          this.followLoading = false;
          this.loadFriendStatus(this.user!.id);
        },
        error: () => {
          this.followLoading = false;
        }
      });
      return;
    }

    this.friendService.followUser(this.user.id).subscribe({
      next: () => {
        this.followLoading = false;
        this.loadFriendStatus(this.user!.id);
      },
      error: () => {
        this.followLoading = false;
      }
    });
  }

  private loadUser(identifier: string): void {
    this.loading = true;

    const isNumericId = /^\d+$/.test(identifier);

    if (isNumericId) {
      this.userService.getById(Number(identifier)).subscribe({
        next: user => this.handleUserSuccess(user),
        error: () => this.handleUserError()
      });
    } else {
      const username = identifier.startsWith('@') ? identifier.slice(1) : identifier;
      this.userService.getByUsername(username).subscribe({
        next: user => this.handleUserSuccess(user),
        error: () => this.handleUserError()
      });
    }
  }

  private handleUserSuccess(user: UserPublic): void {
    this.user = user;
    if (this.isOwnProfile && this.currentUser) {
      this.editEmail = this.currentUser.email ?? '';
    }
    this.loading = false;
    this.loadFriendStatus(user.id);
  }

  private handleUserError(): void {
    this.user = null;
    this.friendStatus = null;
    this.loading = false;
  }

  private mapCurrentUser(user: User): UserPublic {
    return {
      id: user.id,
      username: user.username,
      firstName: user.firstName,
      lastName: user.lastName,
      profilePictureOriginalUrl: user.profilePictureOriginalUrl,
      profilePictureAvatarUrl: user.profilePictureAvatarUrl,
      profilePictureWebpUrl: user.profilePictureWebpUrl,
      profilePictureThumbnailUrl: user.profilePictureThumbnailUrl
    };
  }

  private setCurrentUserProfile(): void {
    if (!this.currentUser) {
      this.user = null;
      this.loading = true;
      return;
    }

    this.user = this.mapCurrentUser(this.currentUser);
    this.editEmail = this.currentUser.email ?? '';
    this.friendStatus = null;
    this.loading = false;
  }

  private loadFriendStatus(userId: number): void {
    this.friendService.getFriendStatus(userId).subscribe({
      next: status => this.friendStatus = status,
      error: () => this.friendStatus = null
    });
  }
}
