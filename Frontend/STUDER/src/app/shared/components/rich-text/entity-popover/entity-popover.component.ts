import { Component, Input, ChangeDetectionStrategy, OnInit, OnChanges, SimpleChanges, ChangeDetectorRef } from '@angular/core';
import { NgClass } from '@angular/common';
import { EntityCacheService } from '../../../services/entity-cache.service';

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

  constructor(private cacheService: EntityCacheService, private cdr: ChangeDetectorRef) {}

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
          data = this.generateMockUserData(this.value);
          this.cacheService.setUser(this.value, data);
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
          data = this.generateMockCourseData(this.value);
          this.cacheService.setCourse(this.value, data);
        }
        break;
      case 'contest':
        data = this.cacheService.getContest(this.value);
        if (!data) {
          data = this.generateMockContestData(this.value);
          this.cacheService.setContest(this.value, data);
        }
        break;
      case 'block':
        data = this.cacheService.getBlock(this.value);
        if (!data) {
          data = this.generateMockBlockData(this.value);
          this.cacheService.setBlock(this.value, data);
        }
        break;
    }

    this.popoverData = data;
  }

  private generateMockUserData(username: string): EntityPopoverData {
    return {
      name: username,
      description: `Usuario de la plataforma STUDER`,
      meta: `📊 ${Math.floor(Math.random() * 100)} problemas resueltos`
    };
  }

  private generateMockTagData(tagName: string): EntityPopoverData {
    return {
      name: tagName,
      description: `Etiqueta temática`,
      meta: `📚 ${Math.floor(Math.random() * 500)} publicaciones`
    };
  }

  private generateMockCourseData(courseName: string): EntityPopoverData {
    return {
      name: courseName,
      description: `Curso de programación`,
      meta: `👥 ${Math.floor(Math.random() * 1000)} estudiantes`
    };
  }

  private generateMockContestData(contestName: string): EntityPopoverData {
    return {
      name: contestName,
      description: `Competencia de programación`,
      meta: `⏱️ 45 minutos de duración`
    };
  }

  private generateMockBlockData(blockName: string): EntityPopoverData {
    return {
      name: blockName,
      description: `Bloque de aprendizaje`,
      meta: `📖 ${Math.floor(Math.random() * 10)} lecciones`
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

