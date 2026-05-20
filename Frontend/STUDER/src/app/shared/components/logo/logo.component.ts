import { Component, EventEmitter, Input, Output } from '@angular/core';
import {NgStyle} from '@angular/common';

export type LogoAnimation = 'none' | 'bounce' | 'spin' | 'pulse';

@Component({
  selector: 'studer-logo',
  standalone: true,
  templateUrl: './logo.component.html',
  imports: [
    NgStyle
  ],
  styleUrls: ['./logo.component.css']
})
export class LogoComponent {

  @Input() size = 3;
  @Input() animation: LogoAnimation = 'none';
  @Output() logoClick = new EventEmitter<void>();

  animating = false;

  onClick() {
    if (this.animation !== 'none') {
      this.animating = false;
      setTimeout(() => this.animating = true, 10);
    }
    this.logoClick.emit();
  }
}

