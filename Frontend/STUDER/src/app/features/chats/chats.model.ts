
export interface UserPublicResponseDTO {
  id: number;
  username: string;
  firstName: string;
  lastName: string;
  profilePictureOriginalUrl: string;
  profilePictureAvatarUrl: string;
  profilePictureWebpUrl: string;
  profilePictureThumbnailUrl: string;
}

export interface FriendStatusResponseDTO {
  isFollowing: boolean;
  isFriend: boolean;
}

export interface LastMessageDTO {
  content: string;
  timestamp: string;
  isRead: boolean;
}

export interface ChatSummaryDTO {
  chatId: number;
  otherUser: UserPublicResponseDTO;
  friendStatus: FriendStatusResponseDTO;
  lastMessage: LastMessageDTO;
}

export interface MessageResponseDTO {
  id: number;
  chatId: number;
  senderId: number;
  content: string;
  link: string;
  replyToId: number;
  isRead: boolean;
  createdDatetime: string;
}

export interface MessageRequestDTO {
  content: string;
  replyToId?: number;
}

export interface Page<T> {
  content: T[];
  pageable: any;
  last: boolean;
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
  sort: any;
  first: boolean;
  numberOfElements: number;
  empty: boolean;
}
