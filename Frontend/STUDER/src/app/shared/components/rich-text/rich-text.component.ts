import { Component, Input, Output, EventEmitter, ChangeDetectionStrategy, OnInit, OnChanges, SimpleChanges, ChangeDetectorRef } from '@angular/core';
import { RouterModule } from '@angular/router';
import { NgClass } from '@angular/common';
import { RichTextParserService, TextToken } from '../../services/rich-text-parser.service';
import { EntityPopoverComponent } from './entity-popover/entity-popover.component';

export interface EntityClickEvent {
  type: 'user' | 'tag' | 'course' | 'contest' | 'block';
  value: string;
}

@Component({
  selector: 'studer-rich-text',
  standalone: true,
  imports: [RouterModule, EntityPopoverComponent, NgClass],
  templateUrl: './rich-text.component.html',
  styleUrls: ['./rich-text.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RichTextComponent implements OnInit, OnChanges {
  @Input() text: string = '';
  @Output() entityClick = new EventEmitter<EntityClickEvent>();

  tokens: TextToken[] = [];
  popoverType: 'user' | 'tag' | 'course' | 'contest' | 'block' | null = null;
  popoverValue: string = '';
  popoverPosition: { top: number; left: number } = { top: 0, left: 0 };

  constructor(private parserService: RichTextParserService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.tokens = this.parserService.parseText(this.text);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['text']) {
      this.tokens = this.parserService.parseText(this.text);
      this.cdr.markForCheck();
    }
  }

  getEntityPrefix(type: string): string {
    return this.parserService.getEntityPrefix(type);
  }

  getEntityRoute(type: string, value: string): string {
    const routes: Record<string, string> = {
      'user': `/user/@${value}`,
      'tag': `/tags/${value}`,
      'course': `/courses/${value}`,
      'contest': `/contests/${value}`,
      'block': `/blocks/${value}`
    };
    return routes[type] || '#';
  }

  onEntityClick(event: MouseEvent, type: string, value: string): void {
    event.preventDefault();
    this.entityClick.emit({
      type: type as any,
      value
    });
  }

  showPopover(event: MouseEvent, type: string, value: string): void {
    const target = event.target as HTMLElement;
    const rect = target.getBoundingClientRect();

    this.popoverType = type as any;
    this.popoverValue = value;
    this.popoverPosition = {
      top: rect.bottom + 8,
      left: rect.left
    };
  }

  hidePopover(): void {
    this.popoverType = null;
    this.popoverValue = '';
  }
}

