export interface DiscussionResponseDTO {
  id: number;
  title: string;
  description: string;
  ownerUsername: string;
  tags: string[];
  closed: boolean;
  closedAt: string | null;
  messageCount: number;
  createdAt: string;
  favourite: boolean;
  participationType: 'OWNER' | 'MESSAGED' | 'FAVOURITE' | 'NONE';
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
  senderUsername: string;
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

