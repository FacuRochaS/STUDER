import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../../config/api.config';
import {
  CourseResponseDTO,
  CoursePageResponseDTO,
  CourseCreateRequestDTO,
  CourseUpdateRequestDTO,
  UserCourseBlockRequestDTO,
  UserCourseBlockResponseDTO
} from './course.model';
import { MessageResponseDTO } from '../discussions/discussion.model';

@Injectable({ providedIn: 'root' })
export class CourseService {
  private readonly base = `${API_CONFIG.baseUrl}${API_CONFIG.courses}`;

  constructor(private readonly http: HttpClient) {}

  create(data: CourseCreateRequestDTO, imageFile?: File): Observable<CourseResponseDTO> {
    if (imageFile) {
      const formData = new FormData();
      formData.append('file', imageFile, imageFile.name);
      formData.append('request', new Blob([JSON.stringify(data)], { type: 'application/json' }));
      return this.http.post<CourseResponseDTO>(this.base, formData);
    }
    return this.http.post<CourseResponseDTO>(this.base, data);
  }

  getById(id: number): Observable<CourseResponseDTO> {
    return this.http.get<CourseResponseDTO>(`${this.base}/${id}`);
  }

  getCourses(page = 0, filter = 'recent', tag?: string): Observable<CoursePageResponseDTO> {
    let params = new HttpParams().set('page', page.toString()).set('filter', filter);
    if (tag) params = params.set('tag', tag);
    return this.http.get<CoursePageResponseDTO>(this.base, { params });
  }

  getMyCourses(page = 0): Observable<CoursePageResponseDTO> {
    const params = new HttpParams().set('page', page.toString());
    return this.http.get<CoursePageResponseDTO>(`${this.base}/me`, { params });
  }

  getFavouriteCourses(page = 0): Observable<CoursePageResponseDTO> {
    const params = new HttpParams().set('page', page.toString());
    return this.http.get<CoursePageResponseDTO>(`${this.base}/favourites`, { params });
  }

  update(id: number, data: CourseUpdateRequestDTO): Observable<CourseResponseDTO> {
    return this.http.put<CourseResponseDTO>(`${this.base}/${id}`, data);
  }

  addFavourite(id: number): Observable<MessageResponseDTO> {
    return this.http.post<MessageResponseDTO>(`${this.base}/${id}/favourite`, {});
  }

  removeFavourite(id: number): Observable<MessageResponseDTO> {
    return this.http.delete<MessageResponseDTO>(`${this.base}/${id}/favourite`);
  }

  saveBlockInteraction(data: UserCourseBlockRequestDTO): Observable<UserCourseBlockResponseDTO> {
    return this.http.post<UserCourseBlockResponseDTO>(`${this.base}/blocks/interaction`, data);
  }
}
