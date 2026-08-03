import { UserPublicResponseDTO } from '../chats/chats.model';
import { BlockVersionResponseDTO } from '../blocks/block.model';

export interface CourseBlockRequestDTO {
  blockId: number;
  versionId: number | null;
  order: number;
}

export interface CourseBlockResponseDTO {
  id: number;
  blockId: number;
  blockName: string;
  version: BlockVersionResponseDTO;
  order: number;
  completed?: boolean;
}

export interface CourseResponseDTO {
  id: number;
  owner: UserPublicResponseDTO;
  name: string;
  slug: string;
  tags: string[];
  link: string;
  published: boolean;
  createdDatetime: string;
  lastUpdatedDatetime: string;
  favourite: boolean;
  favouriteCount: number;
  ratingSum: number;
  ratingCount: number;
  blocks: CourseBlockResponseDTO[];
}

export interface CoursePageResponseDTO {
  courses: CourseResponseDTO[];
  totalElements: number;
  hasMore: boolean;
  currentPage: number;
}

export interface CourseCreateRequestDTO {
  name: string;
  tags?: string[];
  link?: string;
  blocks: CourseBlockRequestDTO[];
}

export interface CourseUpdateRequestDTO {
  name?: string;
  slug?: string;
  link?: string;
  tags?: string[];
  blocks?: CourseBlockRequestDTO[];
}

export interface UserCourseBlockRequestDTO {
  courseBlockId: number;
  completed: boolean;
  duration: number;
  attempts: number;
}

export interface UserCourseBlockResponseDTO {
  id: number;
  courseBlockId: number;
  completed: boolean;
  duration: number;
  attempts: number;
}
