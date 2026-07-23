import { Component, Input, Output, EventEmitter, inject, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { CourseResponseDTO } from '../../course.model';
import { CourseService } from '../../course.service';
import { UsernameComponent } from '../../../../shared/components/username/username.component';

@Component({
  selector: 'studer-course-list',
  standalone: true,
  imports: [
    CommonModule,
    TranslateModule,
    UsernameComponent,
  ],
  templateUrl: './course-list.component.html',
  styleUrls: ['./course-list.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CourseListComponent {
  @Input({ required: true }) courses: CourseResponseDTO[] = [];

  @Output() favouriteToggled = new EventEmitter<{ id: number; favourite: boolean }>();
  @Output() courseSelected = new EventEmitter<number>();

  private courseService = inject(CourseService);

  onCourseClick(courseId: number): void {
    this.courseSelected.emit(courseId);
  }

  trackByCourseId(index: number, course: CourseResponseDTO): number {
    return course.id;
  }

  bannerColor(index: number): string {
    const colors = ['primary', 'correct', 'error', 'warning', 'info', 'restriction'];
    return colors[index % colors.length];
  }

  getAverageRating(course: CourseResponseDTO): number {
    if (course.ratingCount === 0) return 0;
    return Math.round((course.ratingSum / course.ratingCount) * 10) / 10;
  }

  getRelativeTime(dateStr: string): string {
    const now = Date.now();
    const date = new Date(dateStr).getTime();
    const diff = now - date;
    const days = Math.floor(diff / 86400000);
    if (days < 1) return 'Hoy';
    if (days === 1) return 'Ayer';
    if (days < 7) return `Hace ${days} días`;
    if (days < 30) return `Hace ${Math.floor(days / 7)} sem`;
    if (days < 365) return `Hace ${Math.floor(days / 30)} mes`;
    return `Hace ${Math.floor(days / 365)} año`;
  }

  toggleFavourite(course: CourseResponseDTO, event: MouseEvent): void {
    event.stopPropagation();
    const newState = !course.favourite;

    const action = newState
      ? this.courseService.addFavourite(course.id)
      : this.courseService.removeFavourite(course.id);

    action.subscribe(() => {
      course.favourite = newState;
      course.favouriteCount += newState ? 1 : -1;
      this.favouriteToggled.emit({ id: course.id, favourite: newState });
    });
  }
}
