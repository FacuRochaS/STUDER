import { ChatSummaryDTO, MessageResponseDTO } from '../chats/chats.model';
import { FriendResponseDTO } from '../friends/friend.model';

export const MOCK_CHATS: ChatSummaryDTO[] = [
  {
    chatId: 1,
    otherUser: {
      id: 2,
      username: 'JaneDoe',
      firstName: 'Jane',
      lastName: 'Doe',
      profilePictureOriginalUrl: 'https://i.pravatar.cc/150?img=1',
      profilePictureAvatarUrl: 'https://i.pravatar.cc/150?img=1',
      profilePictureWebpUrl: 'https://i.pravatar.cc/150?img=1',
      profilePictureThumbnailUrl: 'https://i.pravatar.cc/150?img=1',
    },
    friendStatus: { isFollowing: true, isFriend: true },
    lastMessage: {
      content: 'Hey, how are you?',
      timestamp: new Date(Date.now() - 1000 * 60 * 5).toISOString(),
      isRead: false
    },
  },
  {
    chatId: 2,
    otherUser: {
      id: 3,
      username: 'PeterJones',
      firstName: 'Peter',
      lastName: 'Jones',
      profilePictureOriginalUrl: 'https://i.pravatar.cc/150?img=2',
      profilePictureAvatarUrl: 'https://i.pravatar.cc/150?img=2',
      profilePictureWebpUrl: 'https://i.pravatar.cc/150?img=2',
      profilePictureThumbnailUrl: 'https://i.pravatar.cc/150?img=2',
    },
    friendStatus: { isFollowing: true, isFriend: true },
    lastMessage: {
      content: 'See you tomorrow!',
      timestamp: new Date(Date.now() - 1000 * 60 * 60 * 2).toISOString(),
      isRead: true
    },
  }
];

export const MOCK_FRIENDS: FriendResponseDTO[] = [
  {
    id: 4,
    userId: 4,
    username: 'NewFriend',
    firstName: 'New',
    lastName: 'Friend',
    email: 'new.friend@example.com',
    isFriend: true,
    profilePictureAvatarUrl: 'https://i.pravatar.cc/150?img=3',
    profilePictureThumbnailUrl: 'https://i.pravatar.cc/150?img=3',
  },
  ...MOCK_CHATS.map(c => ({
    id: c.otherUser.id,
    userId: c.otherUser.id,
    username: c.otherUser.username,
    firstName: c.otherUser.firstName,
    lastName: c.otherUser.lastName,
    email: `${c.otherUser.username}@example.com`,
    isFriend: true,
    profilePictureAvatarUrl: c.otherUser.profilePictureAvatarUrl,
    profilePictureThumbnailUrl: c.otherUser.profilePictureThumbnailUrl,
  }))
];

export const MOCK_MESSAGES: { [chatId: number]: MessageResponseDTO[] } = {
  1: [
    { id: 1, chatId: 1, senderId: 2, content: 'Hey, how are you?', createdDatetime: new Date(Date.now() - 1000 * 60 * 6).toISOString(), isRead: true, link: null,  replyToId: 0 },
    { id: 2, chatId: 1, senderId: 1, content: 'I am fine, thanks!', createdDatetime: new Date(Date.now() - 1000 * 60 * 5).toISOString(), isRead: true, link: null, replyToId: 0 },
    { id: 3, chatId: 1, senderId: 2, content: 'Wanna hang out?', createdDatetime: new Date(Date.now() - 1000 * 60 * 4).toISOString(), isRead: false, link: null,  replyToId: 0 },
    { id: 4, chatId: 1, senderId: 1, content: 'Sure, when?', createdDatetime: new Date(Date.now() - 1000 * 60 * 3).toISOString(), isRead: true, link: null, replyToId: 0 },
    { id: 5, chatId: 1, senderId: 2, content: 'Now!', createdDatetime: new Date(Date.now() - 1000 * 60 * 2).toISOString(), isRead: false, link: null,  replyToId: 0 },
    { id: 6, chatId: 1, senderId: 1, content: 'Ok, coming!', createdDatetime: new Date(Date.now() - 1000 * 60 * 1).toISOString(), isRead: true, link: null,  replyToId: 0 },
    { id: 7, chatId: 1, senderId: 2, content: '', createdDatetime: new Date(Date.now() - 1000 * 60 * 1).toISOString(), isRead: true, link: 'https://i.pravatar.cc/150?img=5',  replyToId: 0 },

  ],
  2: [
    { id: 8, chatId: 2, senderId: 3, content: 'See you tomorrow!', createdDatetime: new Date(Date.now() - 1000 * 60 * 60 * 2).toISOString(), isRead: true, link: null,  replyToId: 0 }
  ]
};
