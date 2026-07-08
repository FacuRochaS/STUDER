import { Component, EventEmitter, HostListener, Input, Output, ViewChild, ViewContainerRef, OnInit, OnDestroy, ComponentRef } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'studer-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './modal.component.html',
  styleUrls: ['./modal.component.css']
})
export class ModalComponent implements OnInit, OnDestroy {
  @Input() title = '';
  @Input() childComponent: any;
  @Input() inputs: { [key: string]: any } = {};
  @Input() outputs: { [key: string]: (event: any) => void } = {};
  @Output() close = new EventEmitter<void>();

  @ViewChild('content', { read: ViewContainerRef, static: true }) content!: ViewContainerRef;
  private componentRef?: ComponentRef<any>;

  ngOnInit(): void {
    this.renderChildComponent();
  }

  ngOnDestroy(): void {
    if (this.componentRef) {
      this.componentRef.destroy();
    }
  }

  onBackdropClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('modal__backdrop')) {
      this.close.emit();
    }
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    this.close.emit();
  }

  private renderChildComponent(): void {
    this.content.clear();
    if (!this.childComponent) return;

    this.componentRef = this.content.createComponent(this.childComponent);

    if (this.inputs) {
      for (const key in this.inputs) {
        this.componentRef.instance[key] = this.inputs[key];
      }
    }

    if (this.outputs) {
      for (const key in this.outputs) {
        if (this.componentRef.instance[key] instanceof EventEmitter) {
          (this.componentRef.instance[key] as EventEmitter<any>).subscribe(this.outputs[key]);
        }
      }
    }
  }
}
