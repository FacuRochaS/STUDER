import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../../config/api.config';
import {
  ContestResponseDTO, ContestCreateRequestDTO, ContestCourseResponseDTO,
  CourseRatingRequestDTO, LeaderboardEntryDTO, AchievementResponseDTO,
  DashboardMetricsDTO, StatusChangeRequest
} from './contest.model';
import { CourseResponseDTO, CourseCreateRequestDTO } from '../courses/course.model';
import { MessageResponseDTO } from '../discussions/discussion.model';

@Injectable({ providedIn: 'root' })
export class ContestService {
  private readonly base = `${API_CONFIG.baseUrl}${API_CONFIG.contests}`;
  private readonly adminBase = `${API_CONFIG.baseUrl}${API_CONFIG.admin}`;

  constructor(private readonly http: HttpClient) {}

  // Public
  list(status?: string, page = 0, size = 10): Observable<any> {
    let params = `?page=${page}&size=${size}`;
    if (status) params += `&status=${status}`;
    return this.http.get<any>(`${this.base}${params}`);
  }

  getById(id: number): Observable<ContestResponseDTO> {
    return this.http.get<ContestResponseDTO>(`${this.base}/${id}`);
  }

  getActive(): Observable<ContestResponseDTO[]> {
    return this.http.get<ContestResponseDTO[]>(`${this.base}/active`);
  }

  getUpcoming(): Observable<ContestResponseDTO[]> {
    return this.http.get<ContestResponseDTO[]>(`${this.base}/upcoming`);
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

  getAchievements(): Observable<AchievementResponseDTO[]> {
    return this.http.get<AchievementResponseDTO[]>(`${this.base}/achievements`);
  }

  getAllAchievements(): Observable<AchievementResponseDTO[]> {
    return this.http.get<AchievementResponseDTO[]>(`${this.base}/achievements/all`);
  }

  // Admin
  getDashboard(): Observable<DashboardMetricsDTO> {
    return this.http.get<DashboardMetricsDTO>(`${this.adminBase}/dashboard`);
  }

  createContest(data: ContestCreateRequestDTO): Observable<ContestResponseDTO> {
    return this.http.post<ContestResponseDTO>(`${this.adminBase}/contests`, data);
  }

  updateContest(id: number, data: ContestCreateRequestDTO): Observable<ContestResponseDTO> {
    return this.http.put<ContestResponseDTO>(`${this.adminBase}/contests/${id}`, data);
  }

  deleteContest(id: number): Observable<MessageResponseDTO> {
    return this.http.delete<MessageResponseDTO>(`${this.adminBase}/contests/${id}`);
  }

  changeStatus(id: number, status: string): Observable<MessageResponseDTO> {
    return this.http.patch<MessageResponseDTO>(`${this.adminBase}/contests/${id}/status`, { status });
  }

  finishContest(id: number): Observable<MessageResponseDTO> {
    return this.http.post<MessageResponseDTO>(`${this.adminBase}/contests/${id}/finish`, {});
  }
}
