import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { ActivityContentData, ActivityOption } from '../../interfaces/content.interfaces';

@Component({
  selector: 'studer-activity-viewer',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './activity-viewer.component.html',
  styleUrls: ['./activity-viewer.component.css']
})
export class ActivityViewerComponent implements OnInit {
  @Input() data!: ActivityContentData;
  @Output() completed = new EventEmitter<void>();

  // Estados locales para las respuestas del alumno
  selectedChoices: Record<string, boolean> = {}; // M.Choice
  orderedOptions: ActivityOption[] = [];         // Ordering
  matchingAnswers: Record<string, string> = {};  // Matching (Option ID -> Seleccionado)
  shuffledMatchTexts: string[] = [];             // Matching (Dropdown)

  draggedIndex: number | null = null;
  evaluated = false;
  results: Record<string, boolean> = {};

  ngOnInit(): void {
    if (this.data) this.resetActivity();
  }

  resetActivity(): void {
    this.selectedChoices = {};
    this.matchingAnswers = {};
    this.evaluated = false;
    this.results = {};

    // Si es Ordenar, mezclamos el array inicial
    if (this.data.activityType === 'ordering') {
      this.orderedOptions = [...this.data.options].sort(() => Math.random() - 0.5);
    }
    // Si es Unir, extraemos los textos derechos y los mezclamos para los selects
    else if (this.data.activityType === 'matching') {
      this.shuffledMatchTexts = this.data.options
        .map(o => o.matchText || '')
        .filter(t => t.trim() !== '')
        .sort(() => Math.random() - 0.5);
    }
  }

  // M.Choice
  toggleChoice(id: string): void {
    if (this.evaluated) return;
    this.selectedChoices[id] = !this.selectedChoices[id];
  }

  // Matching
  selectMatch(optionId: string, value: string): void {
    if (this.evaluated) return;
    this.matchingAnswers[optionId] = value;
  }

  // Ordering (HTML5 Drag Drop simple)
  onDragStart(index: number): void {
    if (this.evaluated) return;
    this.draggedIndex = index;
  }
  onDragOver(event: DragEvent): void { event.preventDefault(); }
  onDrop(targetIndex: number): void {
    if (this.evaluated || this.draggedIndex === null) return;
    const item = this.orderedOptions.splice(this.draggedIndex, 1)[0];
    this.orderedOptions.splice(targetIndex, 0, item);
    this.draggedIndex = null;
  }

  // CALIFICACIÓN
  verifyAnswers(): void {
    this.evaluated = true;
    this.results = {};

    if (this.data.activityType === 'multiple_choice') {
      this.data.options.forEach(opt => {
        const selected = !!this.selectedChoices[opt.id];
        const isCorrect = !!opt.isCorrect;
        this.results[opt.id] = selected === isCorrect;
      });
    }
    else if (this.data.activityType === 'ordering') {
      this.orderedOptions.forEach((opt, index) => {
        this.results[opt.id] = opt.orderIndex === (index + 1);
      });
    }
    else if (this.data.activityType === 'matching') {
      this.data.options.forEach(opt => {
        this.results[opt.id] = this.matchingAnswers[opt.id] === opt.matchText;
      });
    }

    if (this.getCorrectCount() === this.data.options.length) {
      this.completed.emit();
    }
  }

  getCorrectCount(): number {
    return Object.values(this.results).filter(isCorrect => isCorrect).length;
  }
}
