import { Component, Input, Output, EventEmitter, inject, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { tap } from 'rxjs';
import { DiscussionResponseDTO, DiscussionMessageResponseDTO } from '../../discussion.model';
import { DiscussionService } from '../../discussion.service';
import { RichTextComponent } from '../../../../shared/rich-text.index';
import { TranslateModule } from '@ngx-translate/core';
import { LoaderComponent } from '../../../../shared/components/loader/loader.component';
import { UsernameComponent } from '../../../../shared/components/username/username.component';
import { RelativeTimePipe } from '../../../../shared/pipes/relative-time.pipe';
import { DiscussionMessageInputComponent } from '../discussion-message-input/discussion-message-input.component';

@Component({
  selector: 'app-discussion-item',
  standalone: true,
  imports: [
    CommonModule,
    RichTextComponent,
    TranslateModule,
    LoaderComponent,
    UsernameComponent,
    RelativeTimePipe,
    DiscussionMessageInputComponent
  ],
  templateUrl: './discussion-item.component.html',
  styleUrls: ['./discussion-item.component.css']
})
export class DiscussionItemComponent {
  @Input({ required: true }) discussion!: DiscussionResponseDTO;
  @Output() favouriteToggled = new EventEmitter<{ id: number, favourite: boolean }>();

  @ViewChild(DiscussionMessageInputComponent) messageInput?: DiscussionMessageInputComponent;

  private discussionService = inject(DiscussionService);

  isExpanded = false;
  isLoadingMessages = false;
  messages: DiscussionMessageResponseDTO[] = [];
  replyingToMessage: DiscussionMessageResponseDTO | null = null;

  toggleExpand(): void {
    this.isExpanded = !this.isExpanded;
    if (this.isExpanded && this.messages.length === 0) {
      this.loadMessages();
    }
  }

  loadMessages(): void {
    this.isLoadingMessages = true;
    this.discussionService.getMessages(this.discussion.id).subscribe({
      next: (response) => {
        this.messages = response.messages;
        this.isLoadingMessages = false;
      },
      error: () => {
        this.isLoadingMessages = false;
      }
    });
  }

  toggleFavourite(event: MouseEvent): void {
    event.stopPropagation();
    const newFavouriteState = !this.discussion.favourite;

    const action = newFavouriteState
      ? this.discussionService.addFavourite(this.discussion.id)
      : this.discussionService.removeFavourite(this.discussion.id);

    action.subscribe(() => {
      this.discussion.favourite = newFavouriteState;
      this.discussion.favouriteCount += newFavouriteState ? 1 : -1;
      this.favouriteToggled.emit({ id: this.discussion.id, favourite: newFavouriteState });
    });
  }

  toggleLike(message: DiscussionMessageResponseDTO): void {
    const isLiked = message.likedByCurrentUser;
    const action$ = isLiked
      ? this.discussionService.unlikeMessage(message.id)
      : this.discussionService.likeMessage(message.id);

    action$.pipe(
      tap(() => {
        message.likedByCurrentUser = !isLiked;
        message.likeCount += isLiked ? -1 : 1;
      })
    ).subscribe();
  }

  startReply(message: DiscussionMessageResponseDTO, event: MouseEvent): void {
    event.stopPropagation();
    this.replyingToMessage = message;
  }

  cancelReply(): void {
    this.replyingToMessage = null;
  }

  handleMessageSent(event: { content: string, file?: File }): void {
    const payload = {
      content: event.content,
      parentMessageId: this.replyingToMessage?.id
    };

    this.discussionService.createMessage(this.discussion.id, payload, event.file)
      .subscribe({
        next: (newMessage) => {
          if (this.replyingToMessage) {
            const parent = this.findMessage(this.messages, this.replyingToMessage.id);
            parent?.children.push(newMessage);
          } else {
            this.messages.push(newMessage);
          }
          this.discussion.messageCount++;
          this.messageInput?.reset();
          this.cancelReply();
        },
        error: () => {
          if (this.messageInput) { this.messageInput.isSending = false; }
        }
      });
  }

  private findMessage(messages: DiscussionMessageResponseDTO[], id: number): DiscussionMessageResponseDTO | null {
    for (const message of messages) {
      if (message.id === id) return message;
      const foundInChildren = this.findMessage(message.children, id);
      if (foundInChildren) return foundInChildren;
    }
    return null;
  }
}
