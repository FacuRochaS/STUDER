import { CourseBlockResponseDTO } from '../courses/course.model';

export interface ContestResponseDTO {
  id: number;
  title: string;
  content: any;
  tags: string[];
  status: string;
  startDate: string;
  changeDate: string;
  endDate: string;
  createdDatetime: string;
}

export interface ContestCreateRequestDTO {
  title: string;
  content: any;
  tags?: string[];
  startDate: string;
}

export interface ContestCourseResponseDTO {
  id: number;
  name: string;
  slug: string;
  tags: string[];
  link: string;
  ratingSum: number;
  ratingCount: number;
  averageRating: number;
  createdDatetime: string;
  blocks: CourseBlockResponseDTO[];
}

export interface CourseRatingRequestDTO {
  courseId: number;
  rating: number;
}

export interface LeaderboardEntryDTO {
  courseId: number;
  courseName: string;
  averageRating: number;
  ratingCount: number;
  likeCount: number;
  score: number;
}
