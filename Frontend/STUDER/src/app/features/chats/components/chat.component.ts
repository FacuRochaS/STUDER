import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import {ChatService} from '../chats.service';
import {ChatSummaryDTO, MessageResponseDTO} from '../chats.model';
import {RichTextComponent} from '../../../shared/rich-text.index';


@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule, RichTextComponent],
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css']
})
export class ChatComponent implements OnInit {
  private chatService = inject(ChatService);

  chats: ChatSummaryDTO[] = [];
  activeChat: ChatSummaryDTO | null = null;
  messages: MessageResponseDTO[] = [];

  activeTab: 'CHAT' | 'FRIENDS' = 'CHAT';
  searchQuery: string = '';
  newMessage: string = '';

  // TODO: Obtener dinámicamente de tu autenticación
  currentUserId: number = 1;
  loading: boolean = false;

  ngOnInit(): void {
    this.loadChats();
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

  loadMessages(chatId: number): void {
    this.chatService.getMessagesByChatId(chatId, 0, 50).subscribe({
      next: (page) => {
        // Invertimos el orden para que se rendericen cronológicamente de arriba a abajo
        this.messages = page.content.reverse();
      }
    });
  }

  sendMessage(): void {
    if (!this.newMessage.trim() || !this.activeChat) return;

    const request = { content: this.newMessage.trim() };

    // UI Optimista
    const optimisticMsg: any = {
      id: Date.now(),
      chatId: this.activeChat.chatId,
      senderId: this.currentUserId,
      content: this.newMessage,
      createdDatetime: new Date().toISOString()
    };
    this.messages.push(optimisticMsg);

    this.newMessage = '';

    this.chatService.sendMessageToChat(this.activeChat.chatId, request).subscribe({
      next: (msg) => {
        const idx = this.messages.findIndex(m => m.id === optimisticMsg.id);
        if (idx !== -1) this.messages[idx] = msg;
      }
    });
  }

  formatUsername(username: string): string {
    return `@${username}`;
  }
}
