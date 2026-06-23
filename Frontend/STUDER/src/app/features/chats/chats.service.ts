import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import {
  ChatSummaryDTO,
  MessageResponseDTO,
  MessageRequestDTO,
  Page
} from './chats.model';
import {API_CONFIG} from '../../config/api.config';

@Injectable({
  providedIn: 'root'
})
export class ChatService {


  private readonly apiUrl = `${API_CONFIG.baseUrl}${API_CONFIG.chats}`;

  constructor(private http: HttpClient) { }

  /**
   * Obtiene todos los chats del usuario actual.
   */
  getChats(): Observable<ChatSummaryDTO[]> {
    return this.http.get<ChatSummaryDTO[]>(this.apiUrl);
  }

  /**
   * Obtiene los mensajes de un chat específico con paginación.
   * @param chatId El ID del chat.
   * @param page Número de página (0 por defecto).
   * @param size Tamaño de la página (20 por defecto).
   */
  getMessagesByChatId(chatId: number, page: number = 0, size: number = 20): Observable<Page<MessageResponseDTO>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<Page<MessageResponseDTO>>(`${this.apiUrl}/${chatId}/messages`, { params });
  }

  /**
   * Envía un mensaje a un usuario. Si no hay chat, el backend lo crea.
   * @param targetUserId ID del usuario destinatario.
   * @param request Datos del mensaje.
   * @param file Archivo adjunto (opcional).
   */
  sendMessageToUser(targetUserId: number, request: MessageRequestDTO, file?: File): Observable<MessageResponseDTO> {
    const formData = new FormData();
    formData.append('request', new Blob([JSON.stringify(request)], { type: 'application/json' }));
    if (file) {
      formData.append('file', file);
    }
    return this.http.post<MessageResponseDTO>(`${this.apiUrl}/user/${targetUserId}`, formData);
  }

  /**
   * Envía un mensaje a un chat existente.
   * @param chatId ID del chat.
   * @param request Datos del mensaje.
   * @param file Archivo adjunto (opcional).
   */
  sendMessageToChat(chatId: number, request: MessageRequestDTO, file?: File): Observable<MessageResponseDTO> {
    const formData = new FormData();
    formData.append('request', new Blob([JSON.stringify(request)], { type: 'application/json' }));
    if (file) {
      formData.append('file', file);
    }
    return this.http.post<MessageResponseDTO>(`${this.apiUrl}/${chatId}/messages`, formData);
  }

  /**
   * Marca todos los mensajes no leídos de un chat como leídos.
   * @param chatId ID del chat.
   */
  markChatAsRead(chatId: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${chatId}/read`, {});
  }
}
