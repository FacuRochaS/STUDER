import { Component, EventEmitter, Output, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { DiscussionMessageResponseDTO } from '../../discussion.model';

@Component({
  selector: 'app-discussion-message-input',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TranslateModule
  ],
  templateUrl: './discussion-message-input.component.html',
  styleUrls: ['./discussion-message-input.component.css']
})
export class DiscussionMessageInputComponent {
  @Input() replyingTo: DiscussionMessageResponseDTO | null = null;
  @Output() messageSent = new EventEmitter<{ content: string, file?: File }>();
  @Output() replyCancelled = new EventEmitter<void>();

  content = '';
  attachedFile: File | null = null;
  isSending = false;

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      this.attachedFile = input.files[0];
    }
  }

  removeAttachment(): void {
    this.attachedFile = null;
  }

  sendMessage(): void {
    if ((!this.content || !this.content.trim()) && !this.attachedFile) {
      return;
    }

    this.isSending = true;
    this.messageSent.emit({ content: this.content, file: this.attachedFile || undefined });
  }

  sendMessageOnEnter(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  cancelReply(): void {
    this.replyCancelled.emit();
  }

  reset(): void {
    this.content = '';
    this.attachedFile = null;
    this.isSending = false;
  }
}
