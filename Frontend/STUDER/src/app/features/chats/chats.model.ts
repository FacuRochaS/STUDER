
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
  timestamp: string; // En Angular las fechas de Java suelen llegar como strings ISO
  isRead: boolean;
}

export interface ChatSummaryDTO {
  chatId: number;
  otherUser: UserPublicResponseDTO;
  friendStatus: FriendStatusResponseDTO;
  lastMessage: LastMessageDTO;
}

export interface MessageRequestDTO {
  content: string;
  link?: string; // Opcional, ya que puede que el usuario no envíe un link/foto
  replyToId?: number; // Opcional
}

export interface DirectMessage {
  id: number;
  chat: any; // Puedes definir una interfaz más estricta si necesitas los datos del chat aquí
  sender: UserPublicResponseDTO; // Asumiendo que el backend devuelve esto o una estructura similar
  content: string;
  link: string;
  replyTo?: DirectMessage; // Autoreferencia
  isRead: boolean;
  createdDatetime: string;
  lastUpdatedDatetime: string;
}

// Interfaz genérica para manejar la paginación de Spring Data
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
