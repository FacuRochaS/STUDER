import { Component, EventEmitter, Input, Output, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { v4 as uuidv4 } from 'uuid';
import { BlockContentItem } from '../interfaces/content.interfaces';
import { TagInputComponent } from '../../../shared/components/tag-input/tag-input.component';

import { TranslateModule } from '@ngx-translate/core';
import {TextCreatorComponent} from '../text/creator/text-creator.component';
import {ActivityCreatorComponent} from '../activity/creator/activity-creator.component';

export type Difficulty = 'EASY' | 'NORMAL' | 'HARD' | 'EXPERT';



@Component({
  selector: 'studer-block-editor',
  standalone: true,
  // Agregamos los componentes al array de imports
  imports: [CommonModule, FormsModule, TranslateModule, TagInputComponent, TextCreatorComponent, ActivityCreatorComponent],
  templateUrl: './block-editor.component.html',
  styleUrls: ['./block-editor.component.css']
})
export class BlockEditorComponent implements OnInit {
  @Input() initialContent: BlockContentItem[] = [];
  @Output() save = new EventEmitter<any>();

  content: BlockContentItem[] = [];
  metadata = {
    name: '',
    slug: '',
    tags: [] as string[],
    difficulty: 'NORMAL' as Difficulty,
    published: false,
  };

  // Nuestra lista estática de tipos soportados
  availableContentTypes: string[] = ['text', 'activity'];

  ngOnInit(): void {
    this.content = JSON.parse(JSON.stringify(this.initialContent));
  }

  generateSlug(): void {
    this.metadata.slug = this.metadata.name
      .toLowerCase()
      .trim()
      .replace(/[^\w\s-]/g, '')
      .replace(/[\s_-]+/g, '-')
      .replace(/^-+|-+$/g, '');
  }

  addComponent(type: string): void {
    if (!type) return;

    // INICIALIZACIÓN SEGURA: Generamos el JSON por defecto según el tipo
    let defaultData: any = {};
    if (type === 'text') {
      defaultData = { paragraphs: [] };
    } else if (type === 'activity') {
      defaultData = {
        activityType: 'multiple_choice',
        statement: [],
        options: [],
        allowRetry: true,
        showFeedback: true
      };
    }

    const newComponent: BlockContentItem = {
      id: uuidv4(),
      type: type as BlockContentItem['type'],
      data: defaultData
    };

    this.content.push(newComponent);
  }

  deleteComponent(id: string): void {
    this.content = this.content.filter(c => c.id !== id);
  }

  moveComponent(index: number, direction: 'up' | 'down'): void {
    if (direction === 'up' && index > 0) {
      [this.content[index - 1], this.content[index]] = [this.content[index], this.content[index - 1]];
    } else if (direction === 'down' && index < this.content.length - 1) {
      [this.content[index + 1], this.content[index]] = [this.content[index], this.content[index + 1]];
    }
  }

  onComponentDataChange(id: string, newData: any): void {
    const component = this.content.find(c => c.id === id);
    if (component) {
      component.data = newData;
    }
  }

  onSave(): void {
    this.save.emit({
      ...this.metadata,
      content: JSON.stringify(this.content)
    });
  }
}
