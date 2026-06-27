import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'studer-text-creator',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: '<textarea [(ngModel)]="text" (ngModelChange)="onTextChange()"></textarea>',
})
export class TextCreatorComponent {
  @Output() contentChange = new EventEmitter<{ type: 'text', text: string }>();
  text = '';

  onTextChange(): void {
    this.contentChange.emit({
      type: 'text',
      text: this.text,
    });
  }
}
