import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { v4 as uuidv4 } from 'uuid';
import { UploadService } from '../../../../core/services/upload.service';
import { ActivityContentData, ActivityOption, RichNodeType } from '../../interfaces/content.interfaces';

@Component({
  selector: 'studer-activity-creator',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './activity-creator.component.html',
  styleUrls: ['./activity-creator.component.css']
})
export class ActivityCreatorComponent implements OnInit {
  @Input() data!: ActivityContentData;
  @Output() dataChange = new EventEmitter<ActivityContentData>();

  uploadingImageIndex: number | null = null;

  constructor(private readonly uploadService: UploadService) {}

  ngOnInit(): void {
    if (!this.data || !this.data.activityType) {
      this.data = { activityType: 'multiple_choice', statement: [], options: [], allowRetry: true, showFeedback: true };
    }
  }

  onTypeChange(): void {
    this.data.options = [];
    this.updateModel();
  }

  addStatementNode(type: RichNodeType): void {
    this.data.statement.push({ type, value: '' });
    this.updateModel();
  }

  removeStatementNode(index: number): void {
    this.data.statement.splice(index, 1);
    this.updateModel();
  }

  onStatementImageSelected(event: Event, index: number): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    this.uploadService.uploadImage(file, 'activity').subscribe({
      next: (res) => {
        this.data.statement[index].value = res.url;
        this.updateModel();
        input.value = '';
      },
    });
  }

  addOption(): void {
    const newOption: ActivityOption = {
      id: uuidv4(),
      text: '',
      imageUrl: null,
      isCorrect: this.data.activityType === 'multiple_choice' ? false : null,
      matchText: this.data.activityType === 'matching' ? '' : null,
      orderIndex: this.data.activityType === 'ordering' ? this.data.options.length + 1 : null
    };
    this.data.options.push(newOption);
    this.updateModel();
  }

  removeOption(index: number): void {
    this.data.options.splice(index, 1);
    if (this.data.activityType === 'ordering') {
      this.data.options.forEach((opt, i) => opt.orderIndex = i + 1);
    }
    this.updateModel();
  }

  toggleCorrect(option: ActivityOption): void {
    option.isCorrect = !option.isCorrect;
    this.updateModel();
  }

  onOptionImageSelected(event: Event, index: number): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    this.uploadingImageIndex = index;
    this.uploadService.uploadImage(file, 'activity').subscribe({
      next: (res) => {
        this.data.options[index].imageUrl = res.url;
        this.uploadingImageIndex = null;
        this.updateModel();
        input.value = '';
      },
      error: () => {
        this.uploadingImageIndex = null;
      },
    });
  }

  updateModel(): void {
    this.dataChange.emit({ ...this.data });
  }
}
