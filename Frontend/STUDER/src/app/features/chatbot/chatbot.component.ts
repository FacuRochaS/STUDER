import { Component, inject, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { ChatbotService, ChatMessage } from './chatbot.service';

@Component({
  selector: 'studer-chatbot',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslatePipe],
  templateUrl: './chatbot.component.html',
  styleUrls: ['./chatbot.component.css']
})
export class ChatbotComponent implements AfterViewChecked {
  private chatbotService = inject(ChatbotService);
  private translate = inject(TranslateService);

  @ViewChild('chatBody') private chatBody!: ElementRef;
  @ViewChild('chatInput') private chatInput!: ElementRef;

  isOpen = false;
  isMinimized = false;
  messages: ChatMessage[] = [];
  userInput = '';
  loading = false;
  conversationCount = 0;

  ngAfterViewChecked(): void {
    this.scrollToBottom();
  }

  toggle(): void {
    if (!this.isOpen) {
      this.isOpen = true;
      this.isMinimized = false;
      if (this.messages.length === 0) {
        this.addGreeting();
      }
      setTimeout(() => this.chatInput?.nativeElement?.focus(), 100);
    } else {
      this.isMinimized = !this.isMinimized;
    }
  }

  close(): void {
    this.isOpen = false;
  }

  sendMessage(): void {
    const text = this.userInput.trim();
    if (!text || this.loading) return;

    this.messages.push({ role: 'user', text, timestamp: new Date() });
    this.userInput = '';
    this.loading = true;
    this.conversationCount++;

    this.chatbotService.sendMessage(text).subscribe({
      next: (response) => {
        const reply = response?.message?.content || response?.response || response?.output || '';
        this.messages.push({ role: 'assistant', text: String(reply), timestamp: new Date() });
        this.loading = false;
      },
      error: () => {
        this.translate.get('chatbot.error').subscribe((t: string) => {
          this.messages.push({ role: 'assistant', text: t, timestamp: new Date() });
          this.loading = false;
        });
      },
    });
  }

  onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  /** Formats text with basic markdown-like rendering */
  formatText(text: string): string {
    if (text.startsWith('chatbot.')) return text;
    return text
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/```(\w*)\n?([\s\S]*?)```/g, '<pre class="chat-code"><code>$2</code></pre>')
      .replace(/`([^`]+)`/g, '<code class="chat-inline-code">$1</code>')
      .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
      .replace(/\*(.+?)\*/g, '<em>$1</em>')
      .replace(/\n/g, '<br>')
      .replace(/(https?:\/\/[^\s<]+)/g, '<a href="$1" target="_blank" rel="noopener">$1</a>');
  }

  private addGreeting(): void {
    this.translate.get('chatbot.greeting').subscribe((t: string) => {
      this.messages.push({ role: 'assistant', text: t, timestamp: new Date() });
    });
  }

  private scrollToBottom(): void {
    try {
      const el = this.chatBody?.nativeElement;
      if (el) el.scrollTop = el.scrollHeight;
    } catch {}
  }
}
