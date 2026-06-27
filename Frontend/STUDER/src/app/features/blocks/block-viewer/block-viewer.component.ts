import { Component, Input, ViewChild, ViewContainerRef, ComponentFactoryResolver, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TextViewerComponent } from '../text-viewer/text-viewer.component';

@Component({
  selector: 'studer-block-viewer',
  standalone: true,
  imports: [CommonModule],
  template: '<ng-container #container></ng-container>',
})
export class BlockViewerComponent implements OnInit, OnChanges {
  @Input() content: any[] = [];
  @ViewChild('container', { read: ViewContainerRef, static: true }) container!: ViewContainerRef;

  private componentMap = {
    text: TextViewerComponent,
    // Future components will be registered here
  };

  constructor(private resolver: ComponentFactoryResolver) {}

  ngOnInit(): void {
    this.renderContent();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['content']) {
      this.renderContent();
    }
  }

  private renderContent(): void {
    this.container.clear();
    if (!this.content) return;

    for (const item of this.content) {
      const componentType = this.componentMap[item.type as keyof typeof this.componentMap];
      if (componentType) {
        const factory = this.resolver.resolveComponentFactory(componentType);
        const componentRef = this.container.createComponent(factory);
        (componentRef.instance as any).data = item;
      }
    }
  }
}
