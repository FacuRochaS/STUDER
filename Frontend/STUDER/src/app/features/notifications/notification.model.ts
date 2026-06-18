export type LinkedType =
  | 'COURSE'
  | 'DISCUSSION'
  | 'ACTIVITY'
  | 'MESSAGE'
  | 'USER'
  | 'SYSTEM'
  | 'EVENT';

export interface NotificationResponseDTO {
  id: number;
  title: string;
  message: string;
  createdAt: string;
  type: LinkedType;
  linkedId?: number;
  read: boolean;
}

export interface NotificationPageResponseDTO {
  notifications: NotificationResponseDTO[];
  totalElements: number;
  hasMore: boolean;
  currentPage: number;
}

export interface MessageResponseDTO {
  success: boolean;
  message: string;
}
