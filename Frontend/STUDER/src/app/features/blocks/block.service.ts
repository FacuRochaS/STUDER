import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
    BlockCompleteResponseDTO,
    BlockCompleteTreeResponseDTO,
    BlockCreateRequestDTO,
    BlockForkCreateRequestDTO,
    BlockPageResponseDTO,
    BlockResponseDTO,
    BlockStatsDTO,
    BlockVersionCreateRequestDTO
} from './block.model';
import {MessageResponseDTO} from '../discussions/discussion.model';
import {API_CONFIG} from '../../config/api.config';

@Injectable({
  providedIn: 'root'
})
export class BlockService {

  private readonly base = `${API_CONFIG.baseUrl}${API_CONFIG.blocks}`;


  constructor(private http: HttpClient) { }

  createBlock(request: BlockCreateRequestDTO): Observable<BlockResponseDTO> {
    return this.http.post<BlockResponseDTO>(this.base, request);
  }

  forkBlock(request: BlockForkCreateRequestDTO): Observable<BlockResponseDTO> {
    return this.http.post<BlockResponseDTO>(`${this.base}/fork`, request);
  }

  versionBlock(request: BlockVersionCreateRequestDTO): Observable<BlockResponseDTO> {
    return this.http.post<BlockResponseDTO>(`${this.base}/version`, request);
  }

  getBlock(id: number): Observable<BlockResponseDTO> {
    return this.http.get<BlockResponseDTO>(`${this.base}/${id}`);
  }

  getBlockBySlug(slug: string): Observable<BlockResponseDTO> {
    return this.http.get<BlockResponseDTO>(`${this.base}/slug/${slug}`);
  }

  getBlockByVersion(id: number): Observable<BlockResponseDTO> {
    return this.http.get<BlockResponseDTO>(`${this.base}/version/${id}`);
  }

  getBlocksBySearch(
    page: number,
    tags?: string[],
    orderByLikes?: boolean,
    difficulty?: string,
    user?: string,
    name?: string
  ): Observable<BlockPageResponseDTO> {
    let params = new HttpParams().set('page', page.toString());
    if (tags) {
      tags.forEach(tag => {
        params = params.append('tags', tag);
      });
    }
    if (orderByLikes !== undefined) {
      params = params.set('orderByLikes', orderByLikes.toString());
    }
    if (difficulty) {
      params = params.set('difficulty', difficulty);
    }
    if (user) {
      params = params.set('user', user);
    }
    if (name) {
      params = params.set('name', name);
    }
    return this.http.get<BlockPageResponseDTO>(`${this.base}/search`, { params });
  }

  getBlockTree(id: number): Observable<BlockCompleteTreeResponseDTO> {
    return this.http.get<BlockCompleteTreeResponseDTO>(`${this.base}/tree/${id}`);
  }

  getBlockVersion(id: number): Observable<BlockCompleteResponseDTO> {
    return this.http.get<BlockCompleteResponseDTO>(`${this.base}/versions/${id}`);
  }

  getBlockByUser(id: number, page: number): Observable<BlockPageResponseDTO> {
    const params = new HttpParams().set('page', page.toString());
    return this.http.get<BlockPageResponseDTO>(`${this.base}/user/${id}`, { params });
  }

  getMyBlock(page: number): Observable<BlockPageResponseDTO> {
    const params = new HttpParams().set('page', page.toString());
    return this.http.get<BlockPageResponseDTO>(`${this.base}/me`, { params });
  }

  likeBlock(id: number): Observable<MessageResponseDTO> {
    return this.http.post<MessageResponseDTO>(`${this.base}/${id}/like`, {});
  }

  unlikeBlock(id: number): Observable<MessageResponseDTO> {
    return this.http.delete<MessageResponseDTO>(`${this.base}/${id}/like`);
  }

  getBlockStats(id: number): Observable<BlockStatsDTO> {
    return this.http.get<BlockStatsDTO>(`${this.base}/stats/${id}`);
  }

  exploreBlocks(page: number, filters: {
    query?: string; tags?: string[]; difficulty?: string;
    mine?: boolean; following?: boolean; liked?: boolean;
  }): Observable<BlockPageResponseDTO> {
    let params = new HttpParams().set('page', page.toString());
    if (filters.query) params = params.set('query', filters.query);
    if (filters.tags?.length) filters.tags.forEach(t => params = params.append('tags', t));
    if (filters.difficulty) params = params.set('difficulty', filters.difficulty);
    if (filters.mine) params = params.set('mine', 'true');
    if (filters.following) params = params.set('following', 'true');
    if (filters.liked) params = params.set('liked', 'true');
    return this.http.get<BlockPageResponseDTO>(`${this.base}/explore`, { params });
  }
}
