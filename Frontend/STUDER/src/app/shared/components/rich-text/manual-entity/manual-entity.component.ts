import { Component, Input, Output, EventEmitter, ChangeDetectionStrategy } from '@angular/core';
import { RouterModule } from '@angular/router';
import { NgClass } from '@angular/common';
import { EntityPopoverComponent } from '../entity-popover/entity-popover.component';

export interface ManualEntityClickEvent {
  type: 'user' | 'tag' | 'course' | 'contest' | 'block';
  value: string;
}

@Component({
  selector: 'studer-manual-entity',
  standalone: true,
  imports: [RouterModule, EntityPopoverComponent, NgClass],
  templateUrl: './manual-entity.component.html',
  styleUrls: ['./manual-entity.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ManualEntityComponent {
  @Input({ required: true }) type!: 'user' | 'tag' | 'course' | 'contest' | 'block';
  @Input({ required: true }) value!: string;
  @Output() entityClick = new EventEmitter<ManualEntityClickEvent>();

  popoverType: 'user' | 'tag' | 'course' | 'contest' | 'block' | null = null;
  popoverPosition: { top: number; left: number } = { top: 0, left: 0 };

  getEntityRoute(): string {
    const routes: Record<string, string> = {
      'user': `/user/@${this.value}`,
      'tag': `/tags/${this.value}`,
      'course': `/courses/${this.value}`,
      'contest': `/contests/${this.value}`,
      'block': `/blocks/${this.value}`
    };
    return routes[this.type] || '#';
  }

  onEntityClick(event: MouseEvent): void {
    event.preventDefault();
    this.entityClick.emit({
      type: this.type,
      value: this.value
    });
  }

  showPopover(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    const rect = target.getBoundingClientRect();

    this.popoverType = this.type;
    this.popoverPosition = {
      top: rect.bottom + 8,
      left: rect.left
    };
  }

  hidePopover(): void {
    this.popoverType = null;
  }
}

