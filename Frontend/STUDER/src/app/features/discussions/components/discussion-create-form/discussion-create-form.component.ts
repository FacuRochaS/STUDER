import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { TagInputComponent } from '../../../../shared/components/tag-input/tag-input.component';
import { DiscussionCreateRequestDTO } from '../../discussion.model';

@Component({
  selector: 'app-discussion-create-form',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule, TagInputComponent],
  templateUrl: './discussion-create-form.component.html',
})
export class DiscussionCreateFormComponent {
  @Output() save = new EventEmitter<DiscussionCreateRequestDTO>();
  @Output() cancel = new EventEmitter<void>();

  newDiscussionData: DiscussionCreateRequestDTO = { title: '', description: '', tags: [] };
  isCreating = false;

  onSave(): void {
    if (!this.newDiscussionData.title.trim() || !this.newDiscussionData.description.trim()) {
      return;
    }
    this.isCreating = true;
    this.save.emit(this.newDiscussionData);
  }

  onCancel(): void {
    this.cancel.emit();
  }
}
