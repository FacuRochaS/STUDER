import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { GalleryContentData, GalleryImage, GalleryLayout } from '../../interfaces/content.interfaces';

@Component({
  selector: 'studer-gallery-creator',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './gallery-creator.component.html',
  styleUrls: ['./gallery-creator.component.css']
})
export class GalleryCreatorComponent {
  @Input() data: GalleryContentData = { images: [], layout: 'carousel' };
  @Output() dataChange = new EventEmitter<GalleryContentData>();
  @Output() save = new EventEmitter<GalleryContentData>();

  newImageUrl = '';
  newImageAlt = '';

  addImage(): void {
    if (!this.newImageUrl.trim()) return;
    this.data.images.push({ url: this.newImageUrl.trim(), alt: this.newImageAlt.trim() || 'Gallery image', caption: null });
    this.newImageUrl = '';
    this.newImageAlt = '';
    this.emitChange();
  }

  removeImage(index: number): void {
    this.data.images.splice(index, 1);
    this.emitChange();
  }

  setLayout(layout: GalleryLayout): void {
    this.data.layout = layout;
    this.emitChange();
  }

  emitChange(): void {
    this.dataChange.emit({ ...this.data });
  }
}
