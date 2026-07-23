import { Component, EventEmitter, Input, Output, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { v4 as uuidv4 } from 'uuid';
import { BlockContentItem } from '../interfaces/content.interfaces';
import { TagInputComponent } from '../../../shared/components/tag-input/tag-input.component';

import { TranslateModule } from '@ngx-translate/core';
import {TextCreatorComponent} from '../text/creator/text-creator.component';
import {ActivityCreatorComponent} from '../activity/creator/activity-creator.component';
import {VideoCreatorComponent} from '../video/creator/video-creator.component';
import {GalleryCreatorComponent} from '../gallery/creator/gallery-creator.component';

export type Difficulty = 'EASY' | 'NORMAL' | 'HARD' | 'EXPERT';



@Component({
  selector: 'studer-block-editor',
  standalone: true,
  // Agregamos los componentes al array de imports
  imports: [CommonModule, FormsModule, TranslateModule, TagInputComponent, TextCreatorComponent, ActivityCreatorComponent, VideoCreatorComponent, GalleryCreatorComponent],
  templateUrl: './block-editor.component.html',
  styleUrls: ['./block-editor.component.css']
})
export class BlockEditorComponent implements OnInit {
  currentStep: 1 | 2 = 1;
  @Input() initialContent: BlockContentItem[] = [];
  @Input() mode: string = 'create';
  @Input() blockId?: number;
  @Input() blockName?: string;
  @Input() blockDifficulty?: string;
  @Input() blockTags?: string[];
  @Output() save = new EventEmitter<any>();

  content: BlockContentItem[] = [];
  metadata = {
    name: '',
    tags: [] as string[],
    difficulty: 'NORMAL' as Difficulty,
    published: false,
  };

  changeDescription = '';

  availableContentTypes: string[] = ['text', 'activity', 'video', 'gallery'];

  ngOnInit(): void {
    this.content = JSON.parse(JSON.stringify(this.initialContent));
    if (this.mode === 'edit') {
      this.currentStep = 2;
      this.metadata.name = this.blockName ?? '';
      this.metadata.difficulty = (this.blockDifficulty as Difficulty) || 'NORMAL';
      this.metadata.tags = this.blockTags ?? [];
    }
  }

  goToStep(step: 1 | 2): void {
    this.currentStep = step;
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
    } else if (type === 'video') {
      defaultData = { url: '', platform: 'youtube', startTime: null, autoplay: false, controls: true };
    } else if (type === 'gallery') {
      defaultData = { images: [], layout: 'carousel' };
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
    if (this.mode === 'edit') {
      this.save.emit({
        content: JSON.stringify(this.content),
        changeDescription: this.changeDescription || 'Edited',
        blockId: this.blockId,
        published: this.metadata.published,
      });
    } else {
      this.save.emit({
        ...this.metadata,
        content: JSON.stringify(this.content)
      });
    }
  }
}
