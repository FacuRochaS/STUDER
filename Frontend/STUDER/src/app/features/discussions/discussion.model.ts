import {UserPublicResponseDTO} from '../chats/chats.model';

export interface DiscussionResponseDTO {
  id: number;
  title: string;
  description: string;
  owner: UserPublicResponseDTO;
  tags: string[];
  createdAt: string;
  favourite: boolean;
  messageCount: number;
  participationType: 'OWNER' | 'MESSAGED' | 'FAVOURITE' | 'NONE';
  likeCount: number;
  favouriteCount: number;
}

export interface DiscussionPageResponseDTO {
  discussions: DiscussionResponseDTO[];
  totalElements: number;
  hasMore: boolean;
  currentPage: number;
}

export interface DiscussionCreateRequestDTO {
  title: string;
  description: string;
  tags?: string[];
  imageRef?: string;
}

export interface DiscussionMessageResponseDTO {
  id: number;
  sender: UserPublicResponseDTO;
  content: string;
  imageRef: string;
  createdAt: string;
  likeCount: number;
  likedByCurrentUser: boolean;
  children: DiscussionMessageResponseDTO[];
}

export interface DiscussionMessagePageResponseDTO {
  messages: DiscussionMessageResponseDTO[];
  totalElements: number;
  hasMore: boolean;
  currentPage: number;
}

export interface DiscussionMessageCreateRequestDTO {
  content: string;
  parentMessageId?: number;
  imageRef?: string;
}

export interface MessageResponseDTO {
  success: boolean;
  message: string;
}

