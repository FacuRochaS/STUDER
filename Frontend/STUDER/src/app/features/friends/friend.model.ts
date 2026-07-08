export interface FriendResponseDTO {
  id: number;
  userId: number;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  isFriend: boolean;
  profilePictureAvatarUrl?: string;
  profilePictureThumbnailUrl?: string;
}

export interface FriendsListResponseDTO {
  friends: FriendResponseDTO[];
  totalElements: number;
  hasMore: boolean;
  currentPage: number;
}

export interface FriendStatusResponseDTO {
  isFollowing: boolean;
  isFriend: boolean;
}
