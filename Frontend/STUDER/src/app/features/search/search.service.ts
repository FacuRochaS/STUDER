import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { API_CONFIG } from '../../config/api.config';
import { UserPublic } from '../../features/users/user.model';
import { BlockResponseDTO } from '../../features/blocks/block.model';
import { CourseResponseDTO } from '../../features/courses/course.model';
import { ContestResponseDTO } from '../../features/contest/contest.model';

export type SearchCategory = 'users' | 'blocks' | 'courses' | 'contests';

export interface UnifiedSearchResults {
  users: UserPublic[];
  blocks: BlockResponseDTO[];
  courses: CourseResponseDTO[];
  contests: ContestResponseDTO[];
}

@Injectable({ providedIn: 'root' })
export class SearchService {
  private readonly searchUrl = `${API_CONFIG.baseUrl}/search`;

  constructor(private readonly http: HttpClient) {}

  /** Quick search for header dropdown */
  searchQuick(query: string): Observable<UnifiedSearchResults> {
    return this.http.get<UnifiedSearchResults>(`${this.searchUrl}?query=${enc(query)}&size=5`);
  }

  /** Search a specific category using the unified endpoint */
  searchCategory(category: SearchCategory, query: string): Observable<any[]> {
    return this.searchQuick(query).pipe(map(r => (r as any)[category] || []));
  }
}

function enc(s: string): string { return encodeURIComponent(s); }
