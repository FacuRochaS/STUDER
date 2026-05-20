export type LinkedType =
  | 'COURSE'
  | 'DISCUSSION'
  | 'ACTIVITY'
  | 'MESSAGE'
  | 'USER'
  | 'SYSTEM';

export interface NotificationResponseDTO {
  id: number;
  title: string;
  message: string;
  createdAt: string;
  type: LinkedType;
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

/** Maps notification LinkedType to a route prefix */
export const NOTIFICATION_ROUTE_MAP: Record<LinkedType, string> = {
  COURSE: '/courses',
  DISCUSSION: '/discussions',
  ACTIVITY: '/courses',
  MESSAGE: '/messages',
  USER: '/account',
  SYSTEM: '/home'
};

