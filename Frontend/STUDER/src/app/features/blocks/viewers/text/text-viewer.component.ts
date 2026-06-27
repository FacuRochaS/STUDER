import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TextContentData, TextRun } from '../../interfaces/content.interfaces';

@Component({
  selector: 'studer-text-viewer',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './text-viewer.component.html',
  styleUrls: ['./text-viewer.component.css']
})
export class TextViewerComponent {
  @Input() data: TextContentData = { elements: [] };

  getRunClasses(run: TextRun): { [key: string]: boolean } {
    return {
      'run--bold': !!run.bold,
      'run--italic': !!run.italic,
      'run--underline': !!run.underline,
      'run--strike': !!run.strike,
      'run--code': !!run.code,
    };
  }

  getRunStyles(run: TextRun): { [key: string]: string } {
    const styles: { [key: string]: string } = {};
    if (run.color) {
      styles['color'] = `var(--color-${run.color})`;
    }
    if (run.background) {
      styles['backgroundColor'] = `var(--color-${run.background})`;
    }
    return styles;
  }
}
