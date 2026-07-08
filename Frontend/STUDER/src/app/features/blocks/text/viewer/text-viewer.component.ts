import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TextContentData, TextRunData, ParagraphData } from '../../interfaces/content.interfaces';

@Component({
  selector: 'studer-text-viewer',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './text-viewer.component.html',
  styleUrls: ['./text-viewer.component.css']
})
export class TextViewerComponent {
  @Input() data!: TextContentData;

  getRunClasses(run: TextRunData): Record<string, boolean> {
    return {
      'run--bold': run.bold,
      'run--italic': run.italic,
      'run--underline': run.underline,
      'run--strikethrough': run.strikethrough,
      [`run-size--${run.size}`]: true,
      [`run-color--${run.color}`]: true
    };
  }
}
