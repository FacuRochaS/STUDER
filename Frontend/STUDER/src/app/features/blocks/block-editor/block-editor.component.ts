import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BlockCreateRequestDTO, Difficulty } from '../block.model';
import {TextCreatorComponent} from '../text-creator/text-creator.component';


@Component({
  selector: 'studer-block-editor',
  standalone: true,
  imports: [CommonModule, FormsModule, TextCreatorComponent],
  templateUrl: './block-editor.component.html',
  styleUrls: ['./block-editor.component.css']
})
export class BlockEditorComponent {
  @Input() mode: 'create' | 'edit' | 'fork' | 'version' = 'create';
  @Output() save = new EventEmitter<BlockCreateRequestDTO>();

  metadata: {
    name: string;
    slug: string;
    tags: string[];
    difficulty: Difficulty;
    published: boolean;
  } = {
    name: '',
    slug: '',
    tags: [],
    difficulty: 'NORMAL',
    published: false,
  };

  content: any[] = [];

  onContentChange(item: any, index: number): void {
    this.content[index] = item;
  }

  onSave(): void {
    const finalContent = JSON.stringify(this.content);
    const request: BlockCreateRequestDTO = {
      ...this.metadata,
      content: finalContent,
    };
    this.save.emit(request);
  }
}
