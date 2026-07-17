import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { CourseService } from '../../course.service';
import { CourseResponseDTO } from '../../course.model';
import { CourseSidebarComponent, CourseCategory } from '../course-sidebar/course-sidebar.component';
import { CourseListComponent } from '../course-list/course-list.component';
import { CourseCreateComponent } from '../course-create/course-create.component';
import { LoaderComponent } from '../../../../shared/components/loader/loader.component';
import {
  ExploreFilters,
  ExploreFiltersComponent
} from '../../../discussions/components/explore-filters/explore-filters.component';

@Component({
  selector: 'studer-course-explore',
  standalone: true,
  imports: [
    CommonModule,
    TranslateModule,
    CourseSidebarComponent,
    CourseListComponent,
    CourseCreateComponent,
    LoaderComponent,
    ExploreFiltersComponent,
  ],
  templateUrl: './course-explore.component.html',
  styleUrls: ['./course-explore.component.css'],
})
export class CourseExploreComponent implements OnInit {
  private courseService = inject(CourseService);

  courses: CourseResponseDTO[] = [];
  loading = false;
  selectedCategory: CourseCategory = 'recent';
  showCreateForm = false;

  currentExploreFilters: ExploreFilters = {
    tags: [],
    lastDays: null,
    activityHours: 24
  };

  ngOnInit(): void {
    this.loadCoursesByCategory('recent');
  }

  onCategorySelected(category: CourseCategory): void {
    this.selectedCategory = category;
    if (category === 'create') {
      this.showCreateForm = !this.showCreateForm;
      return;
    }
    this.showCreateForm = false;
    this.loadCoursesByCategory(category);
  }

  private loadCoursesByCategory(category: CourseCategory): void {
    this.loading = true;
    this.courses = [];

    const observable = this.getObservableForCategory(category);

    observable.subscribe({
      next: (response) => {
        this.courses = response.courses;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  private getObservableForCategory(category: CourseCategory) {
    switch (category) {
      case 'yours':
        return this.courseService.getMyCourses();
      case 'favourites':
        return this.courseService.getFavouriteCourses();
      case 'recent':
        return this.courseService.getCourses(0, 'recent');
      case 'popular':
        return this.courseService.getCourses(0, 'popular');
      /*case 'explore':
        return this.courseService.getC(
          0,
          this.currentExploreFilters.tags,
          this.currentExploreFilters.lastDays ?? undefined,
          this.currentExploreFilters.activityHours
        );

       */
      default:
        return this.courseService.getCourses(0, 'recent');
    }
  }

  onFiltersChanged(filters: ExploreFilters): void {
    this.currentExploreFilters = filters;
    if (this.selectedCategory === 'explore') {
      this.loadCoursesByCategory('explore');
    }
  }


}
