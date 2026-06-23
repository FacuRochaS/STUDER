import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { ChatSummaryDTO, MessageResponseDTO } from '../chats/chats.model';
import { FriendResponseDTO } from '../friends/friend.model';
import { MOCK_CHATS, MOCK_FRIENDS, MOCK_MESSAGES } from './mock-data';
import { RichTextComponent } from '../../shared/rich-text.index';

@Component({
  selector: 'app-test-chat',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule, RichTextComponent],
  templateUrl: '../chats/components/chat.component.html',
  styleUrls: ['../chats/components/chat.component.css']
})
export class TestChatComponent implements OnInit {
  chats: ChatSummaryDTO[] = [];
  activeChat: ChatSummaryDTO | null = null;
  messages: MessageResponseDTO[] = [];
  friends: FriendResponseDTO[] = [];

  activeTab: 'CHAT' | 'FRIENDS' = 'CHAT';
  searchQuery: string = '';
  newMessage: string = '';
  selectedFile: File | null = null;
  filePreview: string | ArrayBuffer | null = null;

  currentUserId: number = 1; // Mock current user ID
  loading: boolean = false;

  ngOnInit(): void {
    this.loadChats();
    this.loadFriends();
  }

  loadChats(): void {
    this.loading = true;
    this.chats = MOCK_CHATS;
    this.loading = false;
  }

  loadFriends(): void {
    this.friends = MOCK_FRIENDS;
  }

  setTab(tab: 'CHAT' | 'FRIENDS'): void {
    this.activeTab = tab;
  }

  selectChat(chat: ChatSummaryDTO): void {
    this.activeChat = chat;
    this.loadMessages(chat.chatId);
  }

  selectFriend(friend: FriendResponseDTO): void {
    const existingChat = this.chats.find(chat => chat.otherUser.id === friend.userId);
    if (existingChat) {
      this.selectChat(existingChat);
    } else {
      this.activeChat = {
        chatId: Date.now(), // Temporary ID
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
    }
    this.activeTab = 'CHAT';
  }

  loadMessages(chatId: number): void {
    this.messages = MOCK_MESSAGES[chatId] || [];
  }

  sendMessage(): void {
    if ((!this.newMessage.trim() && !this.selectedFile) || !this.activeChat) return;

    const optimisticMsg: MessageResponseDTO = {
      id: Date.now(),
      chatId: this.activeChat.chatId,
      senderId: this.currentUserId,
      content: this.newMessage,
      createdDatetime: new Date().toISOString(),
      isRead: true,
      link: this.filePreview as string,

      replyToId: 0
    };
    this.messages.push(optimisticMsg);
    this.newMessage = '';
    this.removeSelectedFile();

    const chatInList = this.chats.find(c => c.chatId === this.activeChat?.chatId);
    if (chatInList) {
      chatInList.lastMessage = {
        content: optimisticMsg.content || 'Image',
        timestamp: optimisticMsg.createdDatetime,
        isRead: true
      };
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
}
