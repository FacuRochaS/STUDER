import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../../config/api.config';
import {
  ContestResponseDTO,
  ContestCreateRequestDTO,
  ContestCourseResponseDTO,
  CourseRatingRequestDTO,
  LeaderboardEntryDTO
} from './contest.model';
import { CourseResponseDTO, CourseCreateRequestDTO } from '../courses/course.model';
import { MessageResponseDTO } from '../discussions/discussion.model';

@Injectable({ providedIn: 'root' })
export class ContestService {
  private readonly base = `${API_CONFIG.baseUrl}${API_CONFIG.contests}`;

  constructor(private readonly http: HttpClient) {}

  getById(id: number): Observable<ContestResponseDTO> {
    return this.http.get<ContestResponseDTO>(`${this.base}/${id}`);
  }

  submitCourse(id: number, data: CourseCreateRequestDTO): Observable<CourseResponseDTO> {
    return this.http.post<CourseResponseDTO>(`${this.base}/${id}/submit`, data);
  }

  getRandomValidationCourse(id: number): Observable<ContestCourseResponseDTO> {
    return this.http.get<ContestCourseResponseDTO>(`${this.base}/${id}/validate/random`);
  }

  rateCourse(data: CourseRatingRequestDTO): Observable<MessageResponseDTO> {
    return this.http.post<MessageResponseDTO>(`${this.base}/validate`, data);
  }

  getLeaderboard(id: number): Observable<LeaderboardEntryDTO[]> {
    return this.http.get<LeaderboardEntryDTO[]>(`${this.base}/${id}/leaderboard`);
  }

  createContest(data: ContestCreateRequestDTO): Observable<ContestResponseDTO> {
    return this.http.post<ContestResponseDTO>(`${API_CONFIG.baseUrl}${API_CONFIG.admin}/contests`, data);
  }

  finishContest(id: number): Observable<MessageResponseDTO> {
    return this.http.post<MessageResponseDTO>(`${API_CONFIG.baseUrl}${API_CONFIG.admin}/contests/${id}/finish`, {});
  }
}
