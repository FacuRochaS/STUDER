import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Subject, takeUntil } from 'rxjs';
import { TranslatePipe } from '@ngx-translate/core';
import { DiscussionService } from '../../discussion.service';
import {
  DiscussionResponseDTO,
  DiscussionMessageResponseDTO
} from '../../discussion.model';
import { RelativeTimePipe } from '../../../../shared/pipes/relative-time.pipe';
import { TagComponent } from '../../../../shared/components/tag/tag.component';
import { UsernameComponent } from '../../../../shared/components/username/username.component';
import { LoaderComponent } from '../../../../shared/components/loader/loader.component';

@Component({
  selector: 'studer-discussion-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslatePipe, RelativeTimePipe, TagComponent, UsernameComponent, LoaderComponent],
  templateUrl: './discussion-detail.component.html',
  styleUrls: ['./discussion-detail.component.css']
})
export class DiscussionDetailComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();

  discussionId!: number;
  discussion: DiscussionResponseDTO | null = null;
  messages: DiscussionMessageResponseDTO[] = [];
  loading = true;
  messagesLoading = false;
  currentPage = 0;
  hasMore = false;

  newMessageContent = '';
  replyingTo: DiscussionMessageResponseDTO | null = null;
  sending = false;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly discussionService: DiscussionService
  ) {}

  ngOnInit(): void {
    this.route.params
      .pipe(takeUntil(this.destroy$))
      .subscribe(params => {
        this.discussionId = +params['id'];
        this.loadDiscussion();
        this.loadMessages();
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  goBack(): void {
    this.router.navigate(['/discussions']);
  }

  private loadDiscussion(): void {
    this.loading = true;
    this.discussionService
      .getById(this.discussionId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (disc) => {
          this.discussion = disc;
          this.loading = false;
        },
        error: () => {
          this.loading = false;
        }
      });
  }

  loadMessages(append = false): void {
    this.messagesLoading = true;
    this.discussionService
      .getMessages(this.discussionId, this.currentPage)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (page) => {
          this.messages = append
            ? [...this.messages, ...page.messages]
            : page.messages;
          this.hasMore = page.hasMore;
          this.messagesLoading = false;
        },
        error: () => {
          this.messagesLoading = false;
        }
      });
  }

  loadMoreMessages(): void {
    this.currentPage++;
    this.loadMessages(true);
  }

  setReply(message: DiscussionMessageResponseDTO): void {
    this.replyingTo = message;
  }

  cancelReply(): void {
    this.replyingTo = null;
  }

  sendMessage(): void {
    if (!this.newMessageContent.trim() || this.sending) return;

    this.sending = true;
    this.discussionService
      .createMessage(this.discussionId, {
        content: this.newMessageContent,
        parentMessageId: this.replyingTo?.id
      })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (msg) => {
          if (this.replyingTo) {
            this.replyingTo.children = [...(this.replyingTo.children || []), msg];
          } else {
            this.messages = [msg, ...this.messages];
          }
          this.newMessageContent = '';
          this.replyingTo = null;
          this.sending = false;
        },
        error: () => {
          this.sending = false;
        }
      });
  }

  toggleLike(message: DiscussionMessageResponseDTO, event: MouseEvent): void {
    event.stopPropagation();

    const action$ = message.likedByCurrentUser
      ? this.discussionService.unlikeMessage(message.id)
      : this.discussionService.likeMessage(message.id);

    action$.pipe(takeUntil(this.destroy$)).subscribe({
      next: () => {
        message.likedByCurrentUser = !message.likedByCurrentUser;
        message.likeCount += message.likedByCurrentUser ? 1 : -1;
      }
    });
  }

  toggleFavourite(): void {
    if (!this.discussion) return;

    const action$ = this.discussion.favourite
      ? this.discussionService.removeFavourite(this.discussionId)
      : this.discussionService.addFavourite(this.discussionId);

    action$.pipe(takeUntil(this.destroy$)).subscribe({
      next: () => {
        if (this.discussion) {
          this.discussion.favourite = !this.discussion.favourite;
        }
      }
    });
  }

  closeDiscussion(): void {
    this.discussionService
      .close(this.discussionId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          if (this.discussion) {
            this.discussion.closed = true;
          }
        }
      });
  }
}

