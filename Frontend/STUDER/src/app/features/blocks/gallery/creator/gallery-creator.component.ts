import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { UploadService } from '../../../../core/services/upload.service';
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

  uploading = false;
  newImageAlt = '';

  constructor(private readonly uploadService: UploadService) {}

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    this.uploading = true;
    this.uploadService.uploadImage(file, 'gallery').subscribe({
      next: (res) => {
        this.data.images.push({
          url: res.url,
          alt: this.newImageAlt.trim() || 'Gallery image',
          caption: null,
        });
        this.newImageAlt = '';
        this.uploading = false;
        this.emitChange();
        input.value = '';
      },
      error: () => {
        this.uploading = false;
      },
    });
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
