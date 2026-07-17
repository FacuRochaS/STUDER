import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../../config/api.config';
import {
  PostResponseDTO,
  PostPageResponseDTO,
  PostCreateRequestDTO
} from './feed.model';
import { MessageResponseDTO } from '../discussions/discussion.model';

@Injectable({ providedIn: 'root' })
export class FeedService {
  private readonly base = `${API_CONFIG.baseUrl}${API_CONFIG.feed}`;

  constructor(private readonly http: HttpClient) {}

  create(data: PostCreateRequestDTO): Observable<PostResponseDTO> {
    return this.http.post<PostResponseDTO>(this.base, data);
  }

  getFeed(page = 0, filter = 'recent'): Observable<PostPageResponseDTO> {
    const params = new HttpParams().set('page', page.toString()).set('filter', filter);
    return this.http.get<PostPageResponseDTO>(this.base, { params });
  }

  getUserPosts(userId: number, page = 0): Observable<PostPageResponseDTO> {
    const params = new HttpParams().set('page', page.toString());
    return this.http.get<PostPageResponseDTO>(`${this.base}/user/${userId}`, { params });
  }

  likePost(id: number): Observable<MessageResponseDTO> {
    return this.http.post<MessageResponseDTO>(`${this.base}/${id}/like`, {});
  }

  unlikePost(id: number): Observable<MessageResponseDTO> {
    return this.http.delete<MessageResponseDTO>(`${this.base}/${id}/like`);
  }
}
