import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { TranslatePipe } from '@ngx-translate/core';
import { MessagesService } from '../../messages.service';
import {
  ChatPreviewDTO,
  DirectMessagePageResponseDTO,
  DirectMessageResponseDTO
} from '../../messages.model';
import { FriendService } from '../../../users/friend.service';
import { FriendResponseDTO } from '../../../users/friend.model';
import { AuthStateService } from '../../../../core/auth/auth-state.service';
import { RichTextComponent } from '../../../../shared/components/rich-text/rich-text.component';
import { UserService } from '../../../users/user.service';

@Component({
  selector: 'studer-messages',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslatePipe, RichTextComponent],
  templateUrl: './messages.component.html',
  styleUrls: ['./messages.component.css']
})
export class MessagesComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();

  chats: ChatPreviewDTO[] = [];
  requests: DirectMessageResponseDTO[] = [];
  friends: FriendResponseDTO[] = [];

  activeTab: 'chats' | 'requests' = 'chats';
  searchQuery = '';

  selectedChatUserId: number | null = null;
  selectedChatUsername = '';
  selectedChatAvatar: string | null = null;
  messages: DirectMessageResponseDTO[] = [];
  chatMode: 'friend' | 'request' = 'friend';

  loadingChats = false;
  loadingRequests = false;
  loadingMessages = false;

  newMessage = '';
  currentUserId: number | null = null;

  constructor(
    private readonly messagesService: MessagesService,
    private readonly friendService: FriendService,
    private readonly userService: UserService,
    private readonly authState: AuthStateService,
    private readonly route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.authState.user$
      .pipe(takeUntil(this.destroy$))
      .subscribe(user => this.currentUserId = user?.id ?? null);

    this.loadChats();
    this.loadRequests();
    this.loadFriends();

    this.route.queryParamMap
      .pipe(takeUntil(this.destroy$))
      .subscribe(params => {
        const userId = params.get('userId');
        const username = params.get('username');
        if (username) {
          this.selectedChatUsername = username;
        }
        if (userId) {
          const parsed = Number(userId);
          if (!Number.isNaN(parsed)) {
            this.openChat(parsed);
          }
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  get filteredChats(): ChatPreviewDTO[] {
    const query = this.searchQuery.trim().toLowerCase();
    if (!query) return this.chats;
    return this.chats.filter(chat =>
      chat.username.toLowerCase().includes(query) ||
      `${chat.firstName} ${chat.lastName}`.toLowerCase().includes(query)
    );
  }

  get filteredRequests(): DirectMessageResponseDTO[] {
    const query = this.searchQuery.trim().toLowerCase();
    if (!query) return this.requests;
    return this.requests.filter(req =>
      req.senderUsername.toLowerCase().includes(query) ||
      (req.content ?? '').toLowerCase().includes(query)
    );
  }

  get filteredFriends(): FriendResponseDTO[] {
    const query = this.searchQuery.trim().toLowerCase();
    if (!query) return [];
    return this.friends.filter(friend =>
      friend.username.toLowerCase().includes(query) ||
      `${friend.firstName} ${friend.lastName}`.toLowerCase().includes(query)
    );
  }

  setTab(tab: 'chats' | 'requests'): void {
    this.activeTab = tab;
  }

  openChat(userId: number): void {
    const fromChat = this.chats.find(chat => chat.userId === userId);
    if (fromChat) {
      this.selectedChatUsername = fromChat.username;
      this.selectedChatAvatar = fromChat.profilePictureThumbnailUrl || fromChat.profilePictureAvatarUrl || null;
      this.chatMode = 'friend';
    }

    const fromRequest = this.requests.find(req => req.senderId === userId);
    if (fromRequest && !fromChat) {
      this.selectedChatUsername = fromRequest.senderUsername;
      this.selectedChatAvatar = null;
      this.chatMode = 'request';
    }

    const fromFriend = this.friends.find(friend => friend.userId === userId);
    if (fromFriend && !fromChat && !fromRequest) {
      this.selectedChatUsername = fromFriend.username;
      this.selectedChatAvatar = null;
      this.chatMode = 'friend';
    }

    this.selectedChatUserId = userId;
    if (!fromChat && !fromRequest && !fromFriend) {
      this.chatMode = 'request';
      this.messages = [];
      return;
    }

    this.ensureChatAvatar();

    if (this.chatMode === 'friend') {
      this.loadChatMessages(userId);
    } else {
      this.messages = this.requests.filter(req => req.senderId === userId);
    }
  }

  sendMessage(): void {
    if (!this.selectedChatUserId || Number.isNaN(this.selectedChatUserId)) return;
    const content = this.newMessage.trim();
    if (!content) return;

    this.messagesService.sendMessage({ receiverId: this.selectedChatUserId, content }).subscribe({
      next: message => {
        this.messages = [...this.messages, message];
        this.newMessage = '';
        this.loadChats();
      }
    });
  }

  trackByChat(_: number, chat: ChatPreviewDTO): number {
    return chat.userId;
  }

  trackByRequest(_: number, request: DirectMessageResponseDTO): number {
    return request.id;
  }

  getFriendInitials(friend: FriendResponseDTO): string {
    const first = friend.firstName?.[0] ?? '';
    const fallback = friend.username?.[0] ?? '';
    return (first || fallback).toUpperCase();
  }

  getUsernameInitials(username: string): string {
    return (username?.[0] ?? '').toUpperCase();
  }

  private ensureChatAvatar(): void {
    if (!this.selectedChatUsername || this.selectedChatAvatar) return;
    this.userService.getByUsername(this.selectedChatUsername).subscribe({
      next: user => {
        this.selectedChatAvatar = user.profilePictureThumbnailUrl || user.profilePictureAvatarUrl || null;
      }
    });
  }

  private loadChats(): void {
    this.loadingChats = true;
    this.messagesService.getChats().subscribe({
      next: response => {
        this.chats = response.chats;
        this.loadingChats = false;
      },
      error: () => {
        this.loadingChats = false;
      }
    });
  }

  private loadRequests(): void {
    this.loadingRequests = true;
    this.messagesService.getRequests().subscribe({
      next: response => {
        this.requests = response.messages;
        this.loadingRequests = false;
      },
      error: () => {
        this.loadingRequests = false;
      }
    });
  }

  private loadFriends(): void {
    this.friendService.getFriends().subscribe({
      next: response => this.friends = response.friends,
      error: () => this.friends = []
    });
  }

  private loadChatMessages(userId: number): void {
    this.loadingMessages = true;
    this.messagesService.getChatByUserId(userId).subscribe({
      next: (response: DirectMessagePageResponseDTO) => {
        this.messages = response.messages;
        this.loadingMessages = false;
      },
      error: () => {
        this.messages = [];
        this.loadingMessages = false;
      }
    });
  }
}

