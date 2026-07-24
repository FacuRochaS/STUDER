import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { Subject, takeUntil } from 'rxjs';
import { FeedService } from '../feed/feed.service';
import { PostResponseDTO } from '../feed/feed.model';
import { AuthStateService } from '../../core/auth/auth-state.service';
import { RichTextComponent } from '../../shared/components/rich-text/rich-text.component';
import { LoaderComponent } from '../../shared/components/loader/loader.component';
import { TagComponent } from '../../shared/components/tag/tag.component';
import { UsernameComponent } from '../../shared/components/username/username.component';
import { RelativeTimePipe } from '../../shared/pipes/relative-time.pipe';
import { FeedSidebarComponent } from './feed-sidebar/feed-sidebar.component';
import { TextCreatorComponent } from '../blocks/text/creator/text-creator.component';
import { TextViewerComponent } from '../blocks/text/viewer/text-viewer.component';
import { GalleryCreatorComponent } from '../blocks/gallery/creator/gallery-creator.component';
import { GalleryViewerComponent } from '../blocks/gallery/viewer/gallery-viewer.component';
import { TextContentData, GalleryContentData } from '../blocks/interfaces/content.interfaces';

@Component({
  selector: 'studer-home',
  standalone: true,
  imports: [
    CommonModule, FormsModule, TranslateModule,
    RichTextComponent, LoaderComponent,
    TagComponent, UsernameComponent, RelativeTimePipe,
    FeedSidebarComponent, TextCreatorComponent, TextViewerComponent,
    GalleryCreatorComponent, GalleryViewerComponent,
  ],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();
  private feedService = inject(FeedService);
  private authState = inject(AuthStateService);

  posts: PostResponseDTO[] = [];
  loading = false;
  currentFilter = 'recent';
  currentUserId: number | null = null;
  selectedCategory = 'recent';

  newPostTags: string[] = [];
  showCreateForm = false;
  postContentData: TextContentData = { paragraphs: [] };
  postGalleryData: GalleryContentData = { images: [], layout: 'grid' };

  ngOnInit(): void {
    this.authState.user$.pipe(takeUntil(this.destroy$)).subscribe(u => this.currentUserId = u?.id ?? null);
    this.loadFeed();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onCategorySelected(category: string): void {
    if (category === 'create') {
      this.showCreateForm = !this.showCreateForm;
    } else {
      this.showCreateForm = false;
      this.currentFilter = category;
      this.loadFeed();
    }
  }

  onPostContentChange(data: TextContentData): void {
    this.postContentData = data;
  }

  loadFeed(): void {
    this.loading = true;
    this.posts = [];
    this.feedService.getFeed(0, this.currentFilter).pipe(takeUntil(this.destroy$)).subscribe({
      next: (res) => { this.posts = res.posts; this.loading = false; },
      error: () => this.loading = false
    });
  }

  createPost(): void {
    const paragraphs = this.postContentData.paragraphs.filter(p => p.runs.some(r => r.text.trim() || r.imageUrl));
    const hasText = paragraphs.length > 0;
    const hasGallery = this.postGalleryData.images.length > 0;
    if (!hasText && !hasGallery) return;

    const content: any = {};
    if (hasText) content.paragraphs = paragraphs;
    if (hasGallery) content.gallery = this.postGalleryData;

    this.feedService.create({ content: JSON.stringify(content), tags: this.newPostTags }).pipe(takeUntil(this.destroy$)).subscribe({
      next: () => {
        this.postContentData = { paragraphs: [] };
        this.postGalleryData = { images: [], layout: 'grid' };
        this.newPostTags = [];
        this.showCreateForm = false;
        this.loadFeed();
      },
      error: (err) => {
        console.error('Failed to create post', err);
        alert('Failed to publish post. Check console for details.');
      }
    });
  }

  getPostContent(post: PostResponseDTO): any {
    if (!post.content) return null;
    if (typeof post.content === 'string') {
      try { return JSON.parse(post.content as string); } catch { return null; }
    }
    return post.content;
  }

  getPostText(post: PostResponseDTO): string {
    const content = this.getPostContent(post);
    if (!content) return '';
    if (content.text) return content.text;
    if (content.paragraphs) {
      return content.paragraphs
        .map((p: any) => (p.runs || []).map((r: any) => r.text || '').join(''))
        .join('\n');
    }
    return '';
  }

  toggleLike(post: PostResponseDTO): void {
    const obs = post.likedByCurrentUser
      ? this.feedService.unlikePost(post.id)
      : this.feedService.likePost(post.id);
    obs.pipe(takeUntil(this.destroy$)).subscribe(() => this.loadFeed());
  }
}
