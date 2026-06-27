import { Component, EventEmitter, Input, Output, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { v4 as uuidv4 } from 'uuid';
import { BlockContentItem } from '../interfaces/content.interfaces';
import { ContentRegistryService } from '../registry/content-registry.service';
import { BlockCreateRequestDTO, Difficulty } from '../block.model';

@Component({
  selector: 'studer-block-editor',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './block-editor.component.html',
  styleUrls: ['./block-editor.component.css']
})
export class BlockEditorComponent implements OnInit {
  @Input() initialContent: BlockContentItem[] = [];
  @Output() save = new EventEmitter<Partial<BlockCreateRequestDTO>>();

  content: BlockContentItem[] = [];
  metadata = {
    name: '',
    slug: '',
    tags: [],
    difficulty: 'NORMAL' as Difficulty,
    published: false,
  };

  availableContentTypes: string[] = [];

  constructor(private readonly contentRegistry: ContentRegistryService) {}

  ngOnInit(): void {
    this.content = JSON.parse(JSON.stringify(this.initialContent)); // Deep copy
    this.availableContentTypes = this.contentRegistry.getRegisteredTypes();
  }

  addComponent(type: string, index: number): void {
    const newComponent: BlockContentItem = {
      id: uuidv4(),
      type: type,
      data: {} // Default empty data
    };
    this.content.splice(index + 1, 0, newComponent);
  }

  deleteComponent(id: string): void {
    const index = this.content.findIndex(c => c.id === id);
    if (index > -1) {
      this.content.splice(index, 1);
    }
  }

  moveComponent(id: string, direction: 'up' | 'down'): void {
    const index = this.content.findIndex(c => c.id === id);
    if (index === -1) return;

    const newIndex = direction === 'up' ? index - 1 : index + 1;
    if (newIndex < 0 || newIndex >= this.content.length) return;

    const [item] = this.content.splice(index, 1);
    this.content.splice(newIndex, 0, item);
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
