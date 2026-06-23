import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { ChatService } from '../chats.service';
import { ChatSummaryDTO, MessageResponseDTO } from '../chats.model';
import { AuthStateService } from '../../../core/auth/auth-state.service';
import { Subject, takeUntil } from 'rxjs';
import { FriendResponseDTO } from '../../friends/friend.model';
import { FriendService } from '../../friends/friend.service';
import { NotificationService, AppNotification } from '../../../core/notifications/notification.service';
import { RichTextComponent } from '../../../shared/rich-text.index';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule, RichTextComponent],
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css']
})
export class ChatComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();
  private chatService = inject(ChatService);
  private notificationService = inject(NotificationService);

  chats: ChatSummaryDTO[] = [];
  activeChat: ChatSummaryDTO | null = null;
  messages: MessageResponseDTO[] = [];
  friends: FriendResponseDTO[] = [];

  activeTab: 'CHAT' | 'FRIENDS' = 'CHAT';
  searchQuery: string = '';
  newMessage: string = '';
  selectedFile: File | null = null;
  filePreview: string | ArrayBuffer | null = null;

  currentUserId: number | null = null;
  loading: boolean = false;

  constructor(
    private readonly authState: AuthStateService,
    private readonly friendService: FriendService,
  ) {}

  ngOnInit(): void {
    this.authState.user$
      .pipe(takeUntil(this.destroy$))
      .subscribe(user => this.currentUserId = user?.id ?? null);

    this.loadChats();
    this.loadFriends();
    this.subscribeToNotifications();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private subscribeToNotifications(): void {
    this.notificationService.getNotifications()
      .pipe(takeUntil(this.destroy$))
      .subscribe((notifications: AppNotification[]) => {
        const hasMessageNotification = notifications.some(n => n.type === 'MESSAGE');
        if (hasMessageNotification) {
          this.loadChats();
          if (this.activeChat) {
            this.loadMessages(this.activeChat.chatId);
          }
        }
      });
  }

  loadChats(): void {
    this.loading = true;
    this.chatService.getChats().subscribe({
      next: (data) => {
        this.chats = data;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  setTab(tab: 'CHAT' | 'FRIENDS'): void {
    this.activeTab = tab;
  }

  selectChat(chat: ChatSummaryDTO): void {
    this.activeChat = chat;
    this.loadMessages(chat.chatId);
    this.chatService.markChatAsRead(chat.chatId).subscribe();
  }

  selectFriend(friend: FriendResponseDTO): void {
    const existingChat = this.chats.find(chat => chat.otherUser.id === friend.userId);
    if (existingChat) {
      this.selectChat(existingChat);
      this.activeTab = 'CHAT';
    } else {
      this.activeChat = {
        chatId: 0,
        otherUser: {
          id: friend.userId,
          username: friend.username,
          firstName: friend.firstName,
          lastName: friend.lastName,
          profilePictureOriginalUrl: '',
          profilePictureAvatarUrl: friend.profilePictureAvatarUrl,
          profilePictureWebpUrl: '',
          profilePictureThumbnailUrl: friend.profilePictureThumbnailUrl,
        },
        friendStatus: { isFollowing: true, isFriend: true },
        lastMessage: { content: '', timestamp: '', isRead: true }
      };
      this.messages = [];
      this.activeTab = 'CHAT';
    }
  }

  loadMessages(chatId: number): void {
    this.chatService.getMessagesByChatId(chatId, 0, 50).subscribe({
      next: (page) => {
        this.messages = page.content.reverse();
      }
    });
  }

  sendMessage(): void {
    if ((!this.newMessage.trim() && !this.selectedFile) || !this.activeChat) return;

    const request = { content: this.newMessage.trim() };
    const file = this.selectedFile;

    this.newMessage = '';
    this.removeSelectedFile();

    if (this.activeChat.chatId === 0) {
      this.chatService.sendMessageToUser(this.activeChat.otherUser.id, request, file ?? undefined).subscribe({
        next: () => {
          this.loadChats();
          this.chatService.getChats().subscribe(chats => {
            const newChat = chats.find(c => c.otherUser.id === this.activeChat?.otherUser.id);
            if (newChat) {
              this.selectChat(newChat);
            }
          });
        }
      });
    } else {
      this.chatService.sendMessageToChat(this.activeChat.chatId, request, file ?? undefined).subscribe({
        next: (msg) => {
          const idx = this.messages.findIndex(m => m.id === msg.id);
          if (idx === -1) {
            this.messages.push(msg);
          }
        }
      });
    }
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      this.selectedFile = input.files[0];
      const reader = new FileReader();
      reader.onload = () => this.filePreview = reader.result;
      reader.readAsDataURL(this.selectedFile);
    }
  }

  removeSelectedFile(): void {
    this.selectedFile = null;
    this.filePreview = null;
  }

  formatUsername(username: string): string {
    return `@${username}`;
  }

  private loadFriends(): void {
    this.friendService.getFriends().subscribe({
      next: response => this.friends = response.friends,
      error: () => this.friends = []
    });
  }
}
