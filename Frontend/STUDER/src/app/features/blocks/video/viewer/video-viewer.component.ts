import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { VideoContentData } from '../../interfaces/content.interfaces';

@Component({
  selector: 'studer-video-viewer',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './video-viewer.component.html',
  styleUrls: ['./video-viewer.component.css']
})
export class VideoViewerComponent implements OnChanges {
  @Input() data!: VideoContentData;

  embedUrl: SafeResourceUrl = '';

  constructor(private sanitizer: DomSanitizer) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['data']) {
      this.computeEmbedUrl();
    }
  }

  private computeEmbedUrl(): void {
    if (this.data.platform === 'youtube') {
      const videoId = this.extractYoutubeId(this.data.url);
      const params = new URLSearchParams();
      if (this.data.startTime) params.set('start', String(this.data.startTime));
      if (this.data.autoplay) params.set('autoplay', '1');
      if (!this.data.controls) params.set('controls', '0');
      const qs = params.toString();
      this.embedUrl = this.sanitizer.bypassSecurityTrustResourceUrl(
        `https://www.youtube.com/embed/${videoId}${qs ? '?' + qs : ''}`
      );
    } else if (this.data.platform === 'vimeo') {
      const videoId = this.extractVimeoId(this.data.url);
      this.embedUrl = this.sanitizer.bypassSecurityTrustResourceUrl(`https://player.vimeo.com/video/${videoId}`);
    } else {
      this.embedUrl = '';
    }
  }

  private extractYoutubeId(url: string): string {
    const match = url.match(/(?:youtube\.com\/watch\?v=|youtu\.be\/)([a-zA-Z0-9_-]+)/);
    return match ? match[1] : url;
  }

  private extractVimeoId(url: string): string {
    const match = url.match(/vimeo\.com\/(\d+)/);
    return match ? match[1] : url;
  }
}
