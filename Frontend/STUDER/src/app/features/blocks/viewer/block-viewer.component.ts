import { Component, Input, OnChanges, SimpleChanges, ViewChild, ViewContainerRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BlockContentItem } from '../interfaces/content.interfaces';
import { ContentRegistryService } from '../registry/content-registry.service';

@Component({
  selector: 'studer-block-viewer',
  standalone: true,
  imports: [CommonModule],
  template: '<ng-container #container></ng-container>',
  styleUrls: ['./block-viewer.component.css']
})
export class BlockViewerComponent implements OnChanges {
  @Input() content: BlockContentItem[] = [];
  @ViewChild('container', { read: ViewContainerRef, static: true }) container!: ViewContainerRef;

  constructor(private readonly contentRegistry: ContentRegistryService) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['content']) {
      this.renderContent();
    }
  }

  private renderContent(): void {
    this.container.clear();
    if (!this.content) {
      return;
    }

    for (const item of this.content) {
      const componentType = this.contentRegistry.getViewerComponent(item.type);
      if (componentType) {
        const componentRef = this.container.createComponent(componentType);
        componentRef.instance.data = item.data;
      } else {
        console.warn(`No viewer component registered for type: ${item.type}`);
        // Optionally, render a placeholder for unknown content types
      }
    }
  }
}
