import { CourseBlockResponseDTO } from '../courses/course.model';

export interface ContestResponseDTO {
  id: number;
  title: string;
  description: string;
  content: any;
  tags: string[];
  status: string;
  startDate: string;
  preparationEndDate: string;
  validationEndDate: string;
  preparationDurationHours: number;
  validationDurationHours: number;
  minPoints: number;
  participantCount: number;
  courseCount: number;
  blockCount: number;
  createdDatetime: string;
}

export interface ContestCreateRequestDTO {
  title: string;
  description?: string;
  content?: string;
  tags?: string[];
  startDate: string;
  preparationDurationHours?: number;
  validationDurationHours?: number;
  minPoints?: number;
}

export interface ContestCourseResponseDTO {
  id: number;
  name: string;
  slug: string;
  tags: string[];
  link: string;
  orderIndex: number;
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

export interface AchievementResponseDTO {
  id: number;
  name: string;
  description: string;
  badgeUrl: string;
  category: string;
  criteria: any;
  unlocked: boolean;
  unlockedAt: string;
}

export interface DashboardMetricsDTO {
  totalUsers: number;
  activeToday: number;
  activeThisWeek: number;
  newRegistrations: number;
  totalBlocks: number;
  totalCourses: number;
  totalForks: number;
  totalVersions: number;
  totalLikes: number;
  totalComments: number;
  activeContests: number;
  finishedContests: number;
  difficultyDistribution: { difficulty: string; count: number }[];
  topTags: { tag: string; count: number }[];
  topUsers: { userId: number; username: string; firstName: string; lastName: string; avatarUrl: string; points: number }[];
  blockTypeDistribution: { difficulty: string; count: number }[];
}

export interface StatusChangeRequest {
  status: string;
}
