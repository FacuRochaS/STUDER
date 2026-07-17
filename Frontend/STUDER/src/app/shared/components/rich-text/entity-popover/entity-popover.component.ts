import { Component, Input, ChangeDetectionStrategy, OnInit, OnChanges, SimpleChanges, ChangeDetectorRef } from '@angular/core';
import { NgClass } from '@angular/common';
import { EntityCacheService } from '../../../services/entity-cache.service';
import { UserService } from '../../../../features/users/user.service';
import { UserPublic } from '../../../../features/users/user.model';
import { CourseService } from '../../../../features/courses/course.service';
import { ContestService } from '../../../../features/contest/contest.service';
import { BlockService } from '../../../../features/blocks/block.service';

interface EntityPopoverData {
  name: string;
  description: string;
  icon?: string;
  image?: string;
  meta?: string;
}

@Component({
  selector: 'studer-entity-popover',
  standalone: true,
  imports: [NgClass],
  templateUrl: './entity-popover.component.html',
  styleUrls: ['./entity-popover.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EntityPopoverComponent implements OnInit, OnChanges {
  @Input() type: 'user' | 'tag' | 'course' | 'contest' | 'block' | null = null;
  @Input() value: string = '';
  @Input() position: { top: number; left: number } = { top: 0, left: 0 };

  popoverType: string | null = null;
  popoverData: EntityPopoverData | null = null;

  constructor(
    private cacheService: EntityCacheService,
    private cdr: ChangeDetectorRef,
    private userService: UserService,
    private courseService: CourseService,
    private contestService: ContestService,
    private blockService: BlockService
  ) {}

  ngOnInit(): void {
    this.loadPopoverData();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['type'] || changes['value']) {
      this.loadPopoverData();
      this.cdr.markForCheck();
    }
  }

  private loadPopoverData(): void {
    if (!this.type || !this.value) {
      this.popoverData = null;
      this.popoverType = null;
      return;
    }

    this.popoverType = this.type;

    // Obtener datos del caché o generar mock
    let data: EntityPopoverData | null = null;

    switch (this.type) {
      case 'user':
        data = this.cacheService.getUser(this.value);
        if (!data) {
          this.popoverData = null;
          this.userService.getByUsername(this.value).subscribe({
            next: (user: UserPublic) => {
              const payload: EntityPopoverData = {
                name: user.username,
                description: `${user.firstName} ${user.lastName}`.trim(),
                meta: `ID: ${user.id}`
              };
              this.cacheService.setUser(this.value, payload);
              this.popoverData = payload;
              this.cdr.markForCheck();
            },
            error: () => {
              const fallback: EntityPopoverData = {
                name: this.value,
                description: 'Usuario',
                meta: ''
              };
              this.cacheService.setUser(this.value, fallback);
              this.popoverData = fallback;
              this.cdr.markForCheck();
            }
          });
          return;
        }
        break;
      case 'tag':
        data = this.cacheService.getTag(this.value);
        if (!data) {
          data = this.generateMockTagData(this.value);
          this.cacheService.setTag(this.value, data);
        }
        break;
      case 'course':
        data = this.cacheService.getCourse(this.value);
        if (!data) {
          this.courseService.getById(Number(this.value)).subscribe({
            next: (course) => {
              const payload: EntityPopoverData = {
                name: course.name,
                description: `by ${course.owner.username}`,
                meta: `${course.ratingCount} ratings`
              };
              this.cacheService.setCourse(this.value, payload);
              this.popoverData = payload;
              this.cdr.markForCheck();
            },
            error: () => {
              const fallback = { name: this.value, description: 'Course', meta: '' };
              this.cacheService.setCourse(this.value, fallback);
              this.popoverData = fallback;
              this.cdr.markForCheck();
            }
          });
          return;
        }
        break;
      case 'contest':
        data = this.cacheService.getContest(this.value);
        if (!data) {
          this.contestService.getById(Number(this.value)).subscribe({
            next: (contest) => {
              const payload: EntityPopoverData = {
                name: contest.title,
                description: contest.status,
                meta: `${contest.tags?.join(', ') || ''}`
              };
              this.cacheService.setContest(this.value, payload);
              this.popoverData = payload;
              this.cdr.markForCheck();
            },
            error: () => {
              const fallback = { name: this.value, description: 'Contest', meta: '' };
              this.cacheService.setContest(this.value, fallback);
              this.popoverData = fallback;
              this.cdr.markForCheck();
            }
          });
          return;
        }
        break;
      case 'block':
        data = this.cacheService.getBlock(this.value);
        if (!data) {
          this.blockService.getBlock(Number(this.value)).subscribe({
            next: (block) => {
              const payload: EntityPopoverData = {
                name: block.name,
                description: `by ${block.owner.username}`,
                meta: block.tags?.join(', ') || ''
              };
              this.cacheService.setBlock(this.value, payload);
              this.popoverData = payload;
              this.cdr.markForCheck();
            },
            error: () => {
              const fallback = { name: this.value, description: 'Block', meta: '' };
              this.cacheService.setBlock(this.value, fallback);
              this.popoverData = fallback;
              this.cdr.markForCheck();
            }
          });
          return;
        }
        break;
    }

    this.popoverData = data;
  }

  private generateMockTagData(tagName: string): EntityPopoverData {
    return {
      name: tagName,
      description: `Etiqueta temática`,
      meta: `📚 ${Math.floor(Math.random() * 500)} publicaciones`
    };
  }

  getPrefix(): string {
    const prefixes: Record<string, string> = {
      'user': '@',
      'tag': '#',
      'course': '&',
      'contest': '$',
      'block': '%'
    };
    return prefixes[this.type || ''] || '';
  }
}

