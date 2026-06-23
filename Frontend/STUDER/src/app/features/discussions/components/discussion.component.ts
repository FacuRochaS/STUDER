import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { Subject, takeUntil } from 'rxjs';
import { DiscussionService } from '../discussion.service';
import { DiscussionResponseDTO, DiscussionMessageResponseDTO } from '../discussion.model';
import { RichTextComponent } from '../../../shared/rich-text.index';
import { UserPublic } from '../../users/user.model';

@Component({
  selector: 'app-discussion',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule, RichTextComponent],
  templateUrl: './discussion.component.html',
  styleUrls: ['./discussion.component.css']
})
export class DiscussionComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();
  private discussionService = inject(DiscussionService);
  private route = inject(ActivatedRoute);

  allDiscussions: DiscussionResponseDTO[] = [];
  discussions: DiscussionResponseDTO[] = [];
  activeDiscussion: DiscussionResponseDTO | null = null;
  messages: DiscussionMessageResponseDTO[] = [];

  searchQuery: string = '';
  loading: boolean = false;

  constructor() {}

  ngOnInit(): void {
    this.route.paramMap
      .pipe(takeUntil(this.destroy$))
      .subscribe(params => {
        const discussionId = params.get('id');
        if (discussionId) {
          this.loadDiscussionsAndSelect(Number(discussionId));
        } else {
          this.loadDiscussions();
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadDiscussions(): void {
    this.loading = true;
    this.discussionService.getPublicDiscussions().subscribe({
      next: (data) => {
        this.allDiscussions = data.discussions;
        this.discussions = data.discussions;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  loadDiscussionsAndSelect(discussionId: number): void {
    this.loading = true;
    this.discussionService.getPublicDiscussions().subscribe({
      next: (data) => {
        this.allDiscussions = data.discussions;
        this.discussions = data.discussions;
        const discussionToSelect = this.discussions.find(d => d.id === discussionId);
        if (discussionToSelect) {
          this.selectDiscussion(discussionToSelect);
        }
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  selectDiscussion(discussion: DiscussionResponseDTO): void {
    this.discussionService.getById(discussion.id).subscribe({
      next: (data) => {
        this.activeDiscussion = data;
        this.loadMessages(data.id);
      }
    });
  }

  loadMessages(discussionId: number): void {
    this.discussionService.getMessages(discussionId).subscribe({
      next: (data) => {
        this.messages = data.messages;
      }
    });
  }

  filterResults(): void {
    const query = this.searchQuery.toLowerCase();
    this.discussions = this.allDiscussions.filter(discussion =>
      discussion.title.toLowerCase().includes(query) ||
      discussion.ownerUsername.toLowerCase().includes(query)
    );
  }
}
