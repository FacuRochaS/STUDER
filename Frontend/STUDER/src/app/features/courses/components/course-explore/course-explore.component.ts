import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { CourseService } from '../../course.service';
import { CourseResponseDTO } from '../../course.model';
import { CourseTabsComponent, ExploreFilters } from '../course-tabs/course-tabs.component';
import { CourseListComponent } from '../course-list/course-list.component';
import { CourseDetailComponent } from '../course-detail/course-detail.component';
import { CourseCreateComponent } from '../course-create/course-create.component';
import { LoaderComponent } from '../../../../shared/components/loader/loader.component';
import { AutoAnimateDirective } from '../../../../shared/directives/auto-animate.directive';

@Component({
  selector: 'studer-course-explore',
  standalone: true,
  imports: [
    CommonModule,
    TranslateModule,
    CourseTabsComponent,
    CourseListComponent,
    CourseDetailComponent,
    CourseCreateComponent,
    LoaderComponent,
    AutoAnimateDirective,
  ],
  templateUrl: './course-explore.component.html',
  styleUrls: ['./course-explore.component.css'],
})
export class CourseExploreComponent implements OnInit {
  private courseService = inject(CourseService);

  courses: CourseResponseDTO[] = [];
  loading = false;
  selectedCategory = 'yours';
  showCreateForm = false;
  selectedCourseId: number | null = null;

  currentExploreFilters: ExploreFilters = {
    tags: [],
    lastDays: null,
    activityHours: 24
  };

  ngOnInit(): void {
    this.loadCoursesByCategory('yours');
  }

  onCourseSelected(courseId: number): void {
    this.selectedCourseId = courseId;
  }

  onDetailClosed(): void {
    this.selectedCourseId = null;
  }

  onCategorySelected(category: string): void {
    this.selectedCategory = category;
    this.selectedCourseId = null;
    this.showCreateForm = false;
    this.loadCoursesByCategory(category);
  }

  toggleCreate(): void {
    this.showCreateForm = !this.showCreateForm;
    if (this.showCreateForm) {
      this.selectedCategory = 'yours';
    }
  }

  onFiltersChanged(filters: ExploreFilters): void {
    this.currentExploreFilters = filters;
    this.loadCoursesByCategory('explore');
  }

  private loadCoursesByCategory(category: string): void {
    this.loading = true;
    this.courses = [];
    const observable = this.getObservableForCategory(category);
    observable.subscribe({
      next: (response) => {
        this.courses = response.courses;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  private getObservableForCategory(category: string) {
    switch (category) {
      case 'yours': return this.courseService.getMyCourses();
      case 'favourites': return this.courseService.getFavouriteCourses();
      case 'recent': return this.courseService.getCourses(0, 'recent');
      case 'popular': return this.courseService.getCourses(0, 'popular');
      case 'explore': return this.courseService.getCourses(0, 'recent', this.currentExploreFilters.tags);
      default: return this.courseService.getCourses(0, 'recent');
    }
  }
}
