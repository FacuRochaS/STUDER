import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, delay } from 'rxjs';
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

  getYourPosts(page = 0): Observable<PostPageResponseDTO> {
    return this.http.get<PostPageResponseDTO>(`${this.base}/yours`, {
      params: new HttpParams().set('page', page.toString())
    }).pipe(delay(300)); // <-- Aplicado aquí
  }

  getFollowingPosts(page = 0): Observable<PostPageResponseDTO> {
    return this.http.get<PostPageResponseDTO>(`${this.base}/following`, {
      params: new HttpParams().set('page', page.toString())
    }).pipe(delay(300));
  }

  getPopularPosts(page = 0): Observable<PostPageResponseDTO> {
    return this.http.get<PostPageResponseDTO>(`${this.base}/popular`, {
      params: new HttpParams().set('page', page.toString())
    }).pipe(delay(300));
  }

  getNewPosts(page = 0): Observable<PostPageResponseDTO> {
    return this.http.get<PostPageResponseDTO>(`${this.base}/new`, {
      params: new HttpParams().set('page', page.toString())
    }).pipe(delay(300));
  }

  getUserPosts(userId: number, page = 0): Observable<PostPageResponseDTO> {
    return this.http.get<PostPageResponseDTO>(`${this.base}/user/${userId}`, {
      params: new HttpParams().set('page', page.toString())
    }).pipe(delay(300));
  }

  likePost(id: number): Observable<MessageResponseDTO> {
    return this.http.post<MessageResponseDTO>(`${this.base}/${id}/like`, {});
  }

  unlikePost(id: number): Observable<MessageResponseDTO> {
    return this.http.delete<MessageResponseDTO>(`${this.base}/${id}/like`);
  }
}
