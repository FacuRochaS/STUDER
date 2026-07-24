import {UserPublicResponseDTO} from '../chats/chats.model';


export type Difficulty =
  | 'EASY'
  | 'NORMAL'
  | 'HARD'
  | 'EXPERT'


export interface BlockCompleteResponseDTO{
  id: number
  createdDatetime: string
  lastUpdatedDatetime: string
  owner: UserPublicResponseDTO
  isFork: boolean
  name: string
  slug: string
  difficulty: Difficulty
  tags: string[]
  versions: BlockVersionResponseDTO[]
  parent: BlockResponseDTO
  likedByCurrentUser?: boolean
  likeCount?: number
}

export interface BlockCompleteTreeResponseDTO {
  parents: BlockCompleteResponseDTO[]
  block: BlockCompleteResponseDTO
  sons: BlockResponseDTO[]
}

export interface BlockCreateRequestDTO {
  tags: string[]
  name: string
  slug: string;
  difficulty:Difficulty
  content:string
  published:boolean
}

export interface BlockForkCreateRequestDTO {
  tags: string[]
  name: string
  slug: string;
  difficulty:Difficulty
  content:string
  blockId:number
  published:boolean
}

export interface BlockPageResponseDTO {
  blocks:BlockResponseDTO[]
  totalElements:number
  hasMore:boolean
  currentPage:number
}

export interface BlockVersionCreateRequestDTO {
  content:string
  changeDescription:string
  blockId:number
  published:boolean
}

export interface BlockVersionResponseDTO {
  id:number
  createdDatetime:string
  lastUpdatedDatetime:string
  content:string
  versionNumber:number
  changeDescription:string
}
export interface BlockStatsDTO {
  likeCount: number;
  forkCount: number;
  versionCount: number;
  usedInCourses: number;
}

export interface BlockResponseDTO {
  id:number
  createdDatetime:string
  lastUpdatedDatetime:string
  owner:UserPublicResponseDTO
  isFork:boolean
  name:string
  slug:string
  difficulty:Difficulty
  tags: string[]
  version:BlockVersionResponseDTO
  likedByCurrentUser?: boolean
  likeCount?: number
}


