import { UserPublicResponseDTO } from '../chats/chats.model';

export interface PostCreateRequestDTO {
  content: string;
  tags?: string[];
}

export interface PostResponseDTO {
  id: number;
  user: UserPublicResponseDTO;
  content: any;
  tags: string[];
  createdDatetime: string;
  likeCount: number;
  likedByCurrentUser: boolean;
}

export interface PostPageResponseDTO {
  posts: PostResponseDTO[];
  totalElements: number;
  hasMore: boolean;
  currentPage: number;
}
