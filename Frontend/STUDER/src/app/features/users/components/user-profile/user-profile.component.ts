import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { TranslatePipe } from '@ngx-translate/core';
import { User, UserPublic } from '../../user.model';
import { UserService } from '../../user.service';
import { FriendService } from '../../../friends/friend.service';
import { FriendStatusResponseDTO } from '../../../friends/friend.model';
import { AuthStateService } from '../../../../core/auth/auth-state.service';
import { RichTextComponent } from '../../../../shared/components/rich-text/rich-text.component';
import { BlockService } from '../../../blocks/block.service';
import { BlockCreateRequestDTO, BlockResponseDTO } from '../../../blocks/block.model';
import { ModalService } from '../../../../shared/services/modal.service';
import {BlockEditorComponent} from '../../../blocks/block-editor/block-editor.component';
import {BlockHeaderComponent} from '../../../blocks/block-header/block-header.component';
import {BlockViewerComponent} from '../../../blocks/block-viewer/block-viewer.component';

@Component({
  selector: 'studer-user-profile',
  standalone: true,
  imports: [
    CommonModule,
    TranslatePipe,
    RichTextComponent,
    BlockHeaderComponent,
    BlockViewerComponent,
  ],
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.css'],
})
export class UserProfileComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();
  private useCurrentUserRoute = false;

  user: UserPublic | null = null;
  loading = false;
  followLoading = false;
  friendStatus: FriendStatusResponseDTO | null = null;
  currentUserId: number | null = null;
  currentUser: User | null = null;
  imagePreviewUrl: string | null = null;

  blocks: BlockResponseDTO[] = [];
  blocksLoading = false;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly userService: UserService,
    private readonly friendService: FriendService,
    private readonly authState: AuthStateService,
    private readonly blockService: BlockService,
    private readonly modalService: ModalService
  ) {}

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
    this.userService.updateMe(undefined, file).subscribe({
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
      title: 'Create New Block',
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

  private mapCurrentUser(user: User): UserPublic {
    return {
      id: user.id,
      username: user.username,
      firstName: user.firstName,
      lastName: user.lastName,
      profilePictureOriginalUrl: user.profilePictureOriginalUrl,
      profilePictureAvatarUrl: user.profilePictureAvatarUrl,
      profilePictureWebpUrl: user.profilePictureWebpUrl,
      profilePictureThumbnailUrl: user.profilePictureThumbnailUrl,
    };
  }

  private setCurrentUserProfile(): void {
    if (!this.currentUser) {
      this.user = null;
      this.loading = true;
      return;
    }
    this.user = this.mapCurrentUser(this.currentUser);
    this.friendStatus = null;
    this.loading = false;
    this.loadUserBlocks(this.currentUser.id);
  }

  private loadFriendStatus(userId: number): void {
    this.friendService.getFriendStatus(userId).subscribe({
      next: (status) => (this.friendStatus = status),
      error: () => (this.friendStatus = null),
    });
  }

  private loadUserBlocks(userId: number): void {
    this.blocksLoading = true;
    this.blockService.getBlockByUser(userId, 0).subscribe({
      next: (page) => {
        this.blocks = page.blocks;
        this.blocksLoading = false;
      },
      error: () => {
        this.blocks = [];
        this.blocksLoading = false;
      },
    });
  }

  parseBlockContent(block: BlockResponseDTO): any[] {
    try {
      if (block.version && block.version.content) {
        return JSON.parse(block.version.content);
      }
    } catch (e) {
      console.error('Error parsing block content', e);
    }
    return [];
  }
}
