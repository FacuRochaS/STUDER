import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TagComponent } from '../tag/tag.component';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'studer-tag-input',
  standalone: true,
  imports: [CommonModule, FormsModule, TagComponent, TranslatePipe],
  templateUrl: './tag-input.component.html',
  styleUrls: ['./tag-input.component.css']
})
export class TagInputComponent {
  @Input() tags: string[] = [];
  @Output() tagsChange = new EventEmitter<string[]>();
  @Input() placeholder = '';

  inputValue = '';

  onKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Enter' || event.key === ',') {
      event.preventDefault();
      this.addCurrentTag();
    }
    if (event.key === 'Backspace' && !this.inputValue && this.tags.length > 0) {
      this.removeTag(this.tags.length - 1);
    }
  }

  addCurrentTag(): void {
    const value = this.inputValue.trim().replace(/^#/, '').toLowerCase();
    if (value && !this.tags.includes(value)) {
      this.tags = [...this.tags, value];
      this.tagsChange.emit(this.tags);
    }
    this.inputValue = '';
  }

  removeTag(index: number): void {
    this.tags = this.tags.filter((_, i) => i !== index);
    this.tagsChange.emit(this.tags);
  }

  onBlur(): void {
    if (this.inputValue.trim()) {
      this.addCurrentTag();
    }
  }
}

