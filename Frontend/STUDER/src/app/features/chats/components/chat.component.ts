import { Component, OnInit, OnDestroy, inject, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { ChatService } from '../chats.service';
import { ChatSummaryDTO, MessageResponseDTO } from '../chats.model';
import { AuthStateService } from '../../../core/auth/auth-state.service';
import { Subject, takeUntil, filter, switchMap, map } from 'rxjs';
import { FriendResponseDTO } from '../../friends/friend.model';
import { FriendService } from '../../friends/friend.service';
import { NewNotificationService } from '../../../core/notifications/new-notification.service';
import { NotificationResponseDTO } from '../../notifications/notification.model';
import { RichTextComponent } from '../../../shared/rich-text.index';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule, RichTextComponent],
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css']
})
export class ChatComponent implements OnInit, OnDestroy, AfterViewChecked {
  private readonly destroy$ = new Subject<void>();
  private chatService = inject(ChatService);
  private notificationService = inject(NewNotificationService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  @ViewChild('scrollMe') private messagesContainer!: ElementRef;
  @ViewChild('fileInput') fileInputRef!: ElementRef<HTMLInputElement>;

  allChats: ChatSummaryDTO[] = [];
  chats: ChatSummaryDTO[] = [];
  activeChat: ChatSummaryDTO | null = null;
  messages: MessageResponseDTO[] = [];
  allFriends: FriendResponseDTO[] = [];
  friends: FriendResponseDTO[] = [];

  activeTab: 'CHAT' | 'FRIENDS' = 'CHAT';
  searchQuery: string = '';
  newMessage: string = '';
  selectedFile: File | null = null;
  filePreview: string | ArrayBuffer | null = null;

  currentUserId: number | null = null;
  loading: boolean = false;
  private needsScroll = false;

  constructor(
    private readonly authState: AuthStateService,
    private readonly friendService: FriendService,
  ) {}

  ngOnInit(): void {
    this.authState.user$
      .pipe(takeUntil(this.destroy$))
      .subscribe(user => this.currentUserId = user?.id ?? null);

    this.loadFriends();
    this.subscribeToNotifications();

    this.route.paramMap.pipe(
      map(params => params.get('chatId')),
      switchMap(chatIdStr => {
        this.loading = true;
        return this.chatService.getChats().pipe(
          map(chats => ({ chats, chatId: chatIdStr ? Number(chatIdStr) : null }))
        );
      }),
      takeUntil(this.destroy$)
    ).subscribe(({ chats, chatId }) => {
      this.allChats = chats;
      this.chats = chats;
      this.filterResults();

      if (chatId !== null) {
        const chatToSelect = this.chats.find(c => c.chatId === chatId);
        if (chatToSelect) {
          this.selectChat(chatToSelect, false); // Don't navigate again
        }
      }
      this.loading = false;
    });
  }

  ngAfterViewChecked(): void {
    if (this.needsScroll) {
      this.scrollToBottom();
      this.needsScroll = false;
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private subscribeToNotifications(): void {
    this.notificationService.getNotifications()
      .pipe(
        filter(notifications => notifications.some(n => n.type === 'MESSAGE')),
        takeUntil(this.destroy$)
      )
      .subscribe(() => {
        this.chatService.getChats().subscribe(chats => {
          this.allChats = chats;
          this.chats = chats;
          this.filterResults();
          // If a chat is active, refresh its messages
          if (this.activeChat) {
            this.loadMessages(this.activeChat.chatId);
          }
        });
      });
  }

  loadChats(): void {
    this.loading = true;
    this.chatService.getChats().subscribe({
      next: (data) => {
        this.allChats = data;
        this.chats = data;
        this.filterResults();
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  setTab(tab: 'CHAT' | 'FRIENDS'): void {
    this.activeTab = tab;
    this.searchQuery = '';
    this.filterResults();
  }

  selectChat(chat: ChatSummaryDTO, navigate = true): void {
    if (navigate) {
      this.router.navigate(['/messages', chat.chatId]);
    }
    this.activeChat = chat;
    this.loadMessages(chat.chatId);
    if (chat.unreadMessages > 0) {
      this.chatService.markChatAsRead(chat.chatId).subscribe(() => {
        const chatInList = this.allChats.find(c => c.chatId === chat.chatId);
        if (chatInList) {
          chatInList.unreadMessages = 0;
        }
      });
    }
  }

  selectFriend(friend: FriendResponseDTO): void {
    const existingChat = this.allChats.find(chat => chat.otherUser.id === friend.userId);
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
        lastMessage: { content: '', timestamp: '', isRead: true },
        unreadMessages: 0
      };
      this.messages = [];
      this.activeTab = 'CHAT';
    }
  }

  loadMessages(chatId: number): void {
    this.chatService.getMessagesByChatId(chatId, 0, 50).subscribe({
      next: (page) => {
        this.messages = page.content.reverse();
        this.needsScroll = true;
      }
    });
  }

  sendMessage(): void {
    if ((!this.newMessage.trim() && !this.selectedFile) || !this.activeChat) return;

    const request = { content: this.newMessage.trim() };
    const file = this.selectedFile;
    const currentActiveChat = this.activeChat;

    this.newMessage = '';
    this.removeSelectedFile();

    if (currentActiveChat.chatId === 0) {
      this.chatService.sendMessageToUser(currentActiveChat.otherUser.id, request, file ?? undefined).subscribe({
        next: (msg) => {
          this.router.navigate(['/messages', msg.chatId]);
        }
      });
    } else {
      this.chatService.sendMessageToChat(currentActiveChat.chatId, request, file ?? undefined).subscribe({
        next: (msg) => {
          const idx = this.messages.findIndex(m => m.id === msg.id);
          if (idx === -1) {
            this.messages.push(msg);
            this.needsScroll = true;
          }
          const chatInList = this.allChats.find(c => c.chatId === currentActiveChat.chatId);
          if (chatInList) {
            chatInList.lastMessage = {
              content: msg.content || 'File',
              timestamp: msg.createdDatetime,
              isRead: true,
              senderId: msg.senderId
            };
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
      input.value = '';
    }
  }

  removeSelectedFile(): void {
    this.selectedFile = null;
    this.filePreview = null;
  }

  formatUsername(username: string): string {
    return `@${username}`;
  }

  filterResults(): void {
    const query = this.searchQuery.toLowerCase();
    if (this.activeTab === 'CHAT') {
      this.chats = this.allChats.filter(chat =>
        chat.otherUser.username.toLowerCase().includes(query) ||
        (chat.otherUser.firstName && chat.otherUser.firstName.toLowerCase().includes(query)) ||
        (chat.otherUser.lastName && chat.otherUser.lastName.toLowerCase().includes(query))
      );
    } else {
      this.friends = this.allFriends.filter(friend =>
        friend.username.toLowerCase().includes(query) ||
        (friend.firstName && friend.firstName.toLowerCase().includes(query)) ||
        (friend.lastName && friend.lastName.toLowerCase().includes(query))
      );
    }
  }

  private loadFriends(): void {
    this.friendService.getFriends().subscribe({
      next: response => {
        this.allFriends = response.friends;
        this.friends = response.friends;
      },
      error: () => {
        this.allFriends = [];
        this.friends = [];
      }
    });
  }

  private scrollToBottom(): void {
    try {
      if (this.messagesContainer) {
        const scrollElement = this.messagesContainer.nativeElement;
        scrollElement.scrollTop = scrollElement.scrollHeight;
      }
    } catch (err) {
      console.error('Could not scroll to bottom:', err);
    }
  }
}
