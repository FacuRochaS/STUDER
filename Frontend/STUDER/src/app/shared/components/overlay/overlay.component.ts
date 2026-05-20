import {
  Component,
  ElementRef,
  EventEmitter,
  HostListener,
  Input,
  Output,
  OnDestroy,
  TemplateRef,
  ViewChild
} from '@angular/core';
import { NgTemplateOutlet } from '@angular/common';

export type OverlayPosition = 'bottom-left' | 'bottom-right' | 'bottom-center';

@Component({
  selector: 'studer-overlay',
  standalone: true,
  imports: [NgTemplateOutlet],
  templateUrl: './overlay.component.html',
  styleUrls: ['./overlay.component.css']
})
export class OverlayComponent implements OnDestroy {
  @Input() position: OverlayPosition = 'bottom-right';
  @Input() contentTemplate!: TemplateRef<unknown>;
  @Output() closed = new EventEmitter<void>();

  isOpen = false;

  @ViewChild('overlayPanel', { static: false }) overlayPanel!: ElementRef<HTMLElement>;

  constructor(private readonly elRef: ElementRef) {}

  toggle(): void {
    this.isOpen = !this.isOpen;
    if (!this.isOpen) {
      this.closed.emit();
    }
  }

  open(): void {
    this.isOpen = true;
  }

  close(): void {
    if (this.isOpen) {
      this.isOpen = false;
      this.closed.emit();
    }
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (this.isOpen && !this.elRef.nativeElement.contains(event.target)) {
      this.close();
    }
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    this.close();
  }

  ngOnDestroy(): void {
    this.close();
  }
}

