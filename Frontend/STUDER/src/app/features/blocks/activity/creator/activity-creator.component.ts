import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { v4 as uuidv4 } from 'uuid';
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

  ngOnInit(): void {
    if (!this.data || !this.data.activityType) {
      this.data = { activityType: 'multiple_choice', statement: [], options: [], allowRetry: true, showFeedback: true };
    }
  }

  onTypeChange(): void {
    this.data.options = []; // Limpiamos opciones al cambiar de tipo
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
    // Reindexar si es de ordenamiento
    if (this.data.activityType === 'ordering') {
      this.data.options.forEach((opt, i) => opt.orderIndex = i + 1);
    }
    this.updateModel();
  }

  toggleCorrect(option: ActivityOption): void {
    option.isCorrect = !option.isCorrect;
    this.updateModel();
  }

  updateModel(): void {
    this.dataChange.emit({ ...this.data });
  }
}
