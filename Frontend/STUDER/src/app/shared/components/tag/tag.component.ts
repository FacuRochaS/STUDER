import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'studer-tag',
  standalone: true,
  imports: [CommonModule],
  template: `<span class="tag">#{{ name }}</span>`,
  styles: [`
    .tag {
      display: inline-block;
      font-size: 0.75rem;
      font-weight: 600;
      color: var(--color-info);
      background: rgba(126, 87, 194, 0.1);
      padding: 0.15rem 0.55rem;
      border-radius: 1rem;
      white-space: nowrap;
      font-family: 'Roboto', Arial, Helvetica, sans-serif;
    }
  `]
})
export class TagComponent {
  @Input({ required: true }) name!: string;
}

