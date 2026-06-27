import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SafeHtmlPipe } from '../../../shared/pipes/safe-html.pipe';

@Component({
  selector: 'studer-text-viewer',
  standalone: true,
  imports: [CommonModule, SafeHtmlPipe],
  template: '<div [innerHTML]="data.text | safeHtml"></div>',
})
export class TextViewerComponent {
  @Input() data: { type: 'text', text: string } = { type: 'text', text: '' };
}
