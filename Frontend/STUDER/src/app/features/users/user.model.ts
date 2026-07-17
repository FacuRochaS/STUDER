export interface LoginRequestDTO {
  username: string;
  password: string;
}

export interface LoginResponseDTO {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface RefreshResponseDTO {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface UserCreateRequestDTO {
  firstName: string;
  lastName: string;
  birthDate: string;
  email: string;
  username: string;
  password: string;
}

export interface UserUpdateRequestDTO {
  email: string | null;
  password: string | null;
}

export interface User {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  points: number;
  role: string;
  profilePictureOriginalUrl: string;
  profilePictureAvatarUrl: string;
  profilePictureWebpUrl: string;
  profilePictureThumbnailUrl: string;
}

export interface UserPublic {
  id: number;
  username: string;
  firstName: string;
  lastName: string;
  points: number;
  role: string;
  profilePictureOriginalUrl: string;
  profilePictureAvatarUrl: string;
  profilePictureWebpUrl: string;
  profilePictureThumbnailUrl: string;
}

export interface UserSearchPageResponse {
  users: UserPublic[];
  totalElements: number;
  hasMore: boolean;
  currentPage: number;
}

