export interface ChatPreviewDTO {
  userId: number;
  username: string;
  firstName: string;
  lastName: string;
  profilePictureAvatarUrl?: string | null;
  profilePictureThumbnailUrl?: string | null;
  lastMessageContent: string | null;
  lastMessageTime: string | null;
  unreadCount: number;
  lastMessageIsFromMe: boolean;
}

export interface ChatListPageResponseDTO {
  chats: ChatPreviewDTO[];
  totalElements: number;
  hasMore: boolean;
  currentPage: number;
}

export interface DirectMessageResponseDTO {
  id: number;
  senderId: number;
  senderUsername: string;
  receiverId: number;
  receiverUsername: string;
  content: string;
  link: string | null;
  sentAt: string;
  isRead: boolean;
  replyToId: number | null;
  replyToContent: string | null;
}

export interface DirectMessagePageResponseDTO {
  messages: DirectMessageResponseDTO[];
  totalElements: number;
  hasMore: boolean;
  currentPage: number;
}

export interface DirectMessageRequestDTO {
  receiverId: number;
  content: string;
  link?: string | null;
  replyToId?: number | null;
}

