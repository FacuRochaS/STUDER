import { Component, Input, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GalleryContentData, GalleryImage } from '../../interfaces/content.interfaces';

@Component({
  selector: 'studer-gallery-viewer',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './gallery-viewer.component.html',
  styleUrls: ['./gallery-viewer.component.css']
})
export class GalleryViewerComponent {
  @Input() data!: GalleryContentData;

  readonly enlargedImage = signal<GalleryImage | null>(null);

  scrollLeft = 0;

  prev(): void {
    this.scrollLeft -= 300;
  }

  next(): void {
    this.scrollLeft += 300;
  }

  enlarge(image: GalleryImage): void {
    this.enlargedImage.set(image);
  }

  closeEnlarged(): void {
    this.enlargedImage.set(null);
  }
}
