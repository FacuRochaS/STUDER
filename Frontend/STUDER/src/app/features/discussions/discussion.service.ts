import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../../config/api.config';
import {
  DiscussionPageResponseDTO,
  DiscussionResponseDTO,
  DiscussionCreateRequestDTO,
  DiscussionMessagePageResponseDTO,
  DiscussionMessageResponseDTO,
  DiscussionMessageCreateRequestDTO,
  MessageResponseDTO
} from './discussion.model';

@Injectable({ providedIn: 'root' })
export class DiscussionService {
  private readonly base = `${API_CONFIG.baseUrl}${API_CONFIG.discussions}`;

  constructor(private readonly http: HttpClient) {}

  /**
   * Obtiene las discusiones públicas paginadas
   */
  getPublicDiscussions(
    page = 0,
    tags?: string[],
    lastDays?: number,
    activityHours = 24
  ): Observable<DiscussionPageResponseDTO> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('activityHours', activityHours.toString());

    if (tags?.length) {
      tags.forEach(t => params = params.append('tags', t));
    }
    if (lastDays !== undefined) {
      params = params.set('lastDays', lastDays.toString());
    }

    return this.http.get<DiscussionPageResponseDTO>(this.base, { params });
  }

  /**
   * Obtiene las discusiones del usuario autenticado
   */
  getMyDiscussions(page = 0): Observable<DiscussionPageResponseDTO> {
    const params = new HttpParams().set('page', page.toString());
    return this.http.get<DiscussionPageResponseDTO>(`${this.base}/me`, { params });
  }

  /**
   * Obtiene una discusión por ID
   */
  getById(discussionId: number): Observable<DiscussionResponseDTO> {
    return this.http.get<DiscussionResponseDTO>(`${this.base}/${discussionId}`);
  }

  /**
   * Crea una nueva discusión
   */
  create(data: DiscussionCreateRequestDTO): Observable<DiscussionResponseDTO> {
    return this.http.post<DiscussionResponseDTO>(this.base, data);
  }

  /**
   * Obtiene los mensajes de una discusión
   */
  getMessages(discussionId: number, page = 0): Observable<DiscussionMessagePageResponseDTO> {
    const params = new HttpParams().set('page', page.toString());
    return this.http.get<DiscussionMessagePageResponseDTO>(
      `${this.base}/${discussionId}/messages`,
      { params }
    );
  }

  /**
   * Crea un mensaje o respuesta en una discusión
   */
  createMessage(
    discussionId: number,
    data: DiscussionMessageCreateRequestDTO
  ): Observable<DiscussionMessageResponseDTO> {
    return this.http.post<DiscussionMessageResponseDTO>(
      `${this.base}/${discussionId}/messages`,
      data
    );
  }

  /**
   * Añade a favoritos
   */
  addFavourite(discussionId: number): Observable<MessageResponseDTO> {
    return this.http.post<MessageResponseDTO>(
      `${this.base}/${discussionId}/favourite`,
      {}
    );
  }

  /**
   * Elimina de favoritos
   */
  removeFavourite(discussionId: number): Observable<MessageResponseDTO> {
    return this.http.delete<MessageResponseDTO>(
      `${this.base}/${discussionId}/favourite`
    );
  }

  /**
   * Da like a un mensaje
   */
  likeMessage(messageId: number): Observable<MessageResponseDTO> {
    return this.http.post<MessageResponseDTO>(
      `${this.base}/messages/${messageId}/like`,
      {}
    );
  }

  /**
   * Quita like de un mensaje
   */
  unlikeMessage(messageId: number): Observable<MessageResponseDTO> {
    return this.http.delete<MessageResponseDTO>(
      `${this.base}/messages/${messageId}/like`
    );
  }

  /**
   * Cierra una discusión (solo el owner)
   */
  close(discussionId: number): Observable<MessageResponseDTO> {
    return this.http.patch<MessageResponseDTO>(
      `${this.base}/${discussionId}/close`,
      {}
    );
  }
}

