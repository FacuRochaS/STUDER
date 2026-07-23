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

  create(data: CourseCreateRequestDTO): Observable<CourseResponseDTO> {
    return this.http.post<CourseResponseDTO>(this.base, data);
  }

  getById(id: number): Observable<CourseResponseDTO> {
    return this.http.get<CourseResponseDTO>(`${this.base}/${id}`);
  }

  getCourses(page = 0, filter = 'recent', tags?: string[]): Observable<CoursePageResponseDTO> {
    let params = new HttpParams().set('page', page.toString()).set('filter', filter);
    if (tags && tags.length > 0) {
      tags.forEach(t => { params = params.append('tags', t); });
    }
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
