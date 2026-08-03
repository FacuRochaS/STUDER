import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { PostResponseDTO } from '../../feed/feed.model';
import { RichTextComponent } from '../../../shared/components/rich-text/rich-text.component';
import { TagComponent } from '../../../shared/components/tag/tag.component';
import { UsernameComponent } from '../../../shared/components/username/username.component';
import { RelativeTimePipe } from '../../../shared/pipes/relative-time.pipe';
import { TextViewerComponent } from '../../blocks/text/viewer/text-viewer.component';
import { GalleryViewerComponent } from '../../blocks/gallery/viewer/gallery-viewer.component';

@Component({
  selector: 'studer-post-card',
  standalone: true,
  imports: [
    CommonModule, TranslateModule, RichTextComponent, TagComponent,
    UsernameComponent, RelativeTimePipe, TextViewerComponent, GalleryViewerComponent,
  ],
  templateUrl: './post-card.component.html',
  styleUrls: ['./post-card.component.css']
})
export class PostCardComponent {
  @Input() post!: PostResponseDTO;
  @Output() like = new EventEmitter<void>();

  get parsedContent(): any {
    if (!this.post?.content) return null;
    if (typeof this.post.content === 'string') {
      try { return JSON.parse(this.post.content); } catch { return null; }
    }
    return this.post.content;
  }

  toggleLike(): void { this.like.emit(); }
}
