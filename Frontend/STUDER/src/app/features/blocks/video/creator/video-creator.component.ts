import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { VideoContentData } from '../../interfaces/content.interfaces';

@Component({
  selector: 'studer-video-creator',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './video-creator.component.html',
  styleUrls: ['./video-creator.component.css']
})
export class VideoCreatorComponent {
  @Input() data: VideoContentData = { url: '', platform: 'youtube', startTime: null, autoplay: false, controls: true };
  @Output() dataChange = new EventEmitter<VideoContentData>();
  @Output() save = new EventEmitter<VideoContentData>();

  emitChange(): void {
    this.dataChange.emit({ ...this.data });
  }
}
