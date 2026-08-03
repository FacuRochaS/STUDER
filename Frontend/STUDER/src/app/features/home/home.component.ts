import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { Subject, takeUntil } from 'rxjs';
import { FeedService } from '../feed/feed.service';
import { PostResponseDTO } from '../feed/feed.model';
import { AuthStateService } from '../../core/auth/auth-state.service';
import { LoaderComponent } from '../../shared/components/loader/loader.component';
import { TextCreatorComponent } from '../blocks/text/creator/text-creator.component';
import { GalleryCreatorComponent } from '../blocks/gallery/creator/gallery-creator.component';
import { TextContentData, GalleryContentData } from '../blocks/interfaces/content.interfaces';
import { PostCardComponent } from './post-card/post-card.component';
import { AutoAnimateDirective } from '../../shared/directives/auto-animate.directive';

@Component({
  selector: 'studer-home',
  standalone: true,
  imports: [
    CommonModule, FormsModule, TranslateModule,
    LoaderComponent, TextCreatorComponent, GalleryCreatorComponent,
    PostCardComponent, AutoAnimateDirective,
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
  loadingMore = false;
  currentFilter = 'yours';
  currentUserId: number | null = null;
  currentPage = 0;
  hasMore = true;
  reachedEnd = false;

  newPostTags: string[] = [];
  showCreateForm = false;
  postContentData: TextContentData = { paragraphs: [] };
  postGalleryData: GalleryContentData = { images: [], layout: 'grid' };

  tabs = [
    { id: 'yours', label: 'feed.yours', icon: 'pi pi-user' },
    { id: 'following', label: 'feed.following', icon: 'pi pi-users' },
    { id: 'popular', label: 'feed.popular', icon: 'pi pi-bolt' },
    { id: 'new', label: 'feed.new_posts', icon: 'pi pi-sparkles' },
  ];

  ngOnInit(): void {
    this.authState.user$.pipe(takeUntil(this.destroy$)).subscribe(u => this.currentUserId = u?.id ?? null);
    this.loadFeed();
  }

  ngOnDestroy(): void { this.destroy$.next(); this.destroy$.complete(); }

  selectTab(id: string): void {
    this.currentFilter = id;
    this.showCreateForm = false;
    this.currentPage = 0;
    this.posts = [];
    this.hasMore = true;
    this.reachedEnd = false;
    this.loadFeed();
  }

  loadFeed(): void {
    if (this.loadingMore) return;
    this.loading = this.currentPage === 0;
    this.loadingMore = this.currentPage > 0;
    const obs = this.getFeedObservable();
    obs.pipe(takeUntil(this.destroy$)).subscribe({
      next: (res) => {
        this.posts = this.currentPage === 0 ? res.posts : [...this.posts, ...res.posts];
        this.hasMore = res.hasMore;
        this.reachedEnd = !res.hasMore && this.posts.length > 0;
        this.currentPage++;
        this.loading = false;
        this.loadingMore = false;
      },
      error: () => { this.loading = false; this.loadingMore = false; }
    });
  }

  private getFeedObservable() {
    switch (this.currentFilter) {
      case 'yours': return this.feedService.getYourPosts(this.currentPage);
      case 'following': return this.feedService.getFollowingPosts(this.currentPage);
      case 'popular': return this.feedService.getPopularPosts(this.currentPage);
      case 'new': return this.feedService.getNewPosts(this.currentPage);
      default: return this.feedService.getYourPosts(this.currentPage);
    }
  }

  loadMore(): void {
    if (!this.hasMore || this.loadingMore) return;
    this.loadFeed();
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
        this.currentPage = 0;
        this.loadFeed();
      },
    });
  }

  toggleLike(post: PostResponseDTO): void {
    const obs = post.likedByCurrentUser
      ? this.feedService.unlikePost(post.id)
      : this.feedService.likePost(post.id);
    obs.pipe(takeUntil(this.destroy$)).subscribe(() => {
      post.likedByCurrentUser = !post.likedByCurrentUser;
      post.likeCount += post.likedByCurrentUser ? 1 : -1;
    });
  }

  onScroll(event: Event): void {
    const el = event.target as HTMLElement;
    if (el.scrollHeight - el.scrollTop - el.clientHeight < 200) {
      this.loadMore();
    }
  }
}
