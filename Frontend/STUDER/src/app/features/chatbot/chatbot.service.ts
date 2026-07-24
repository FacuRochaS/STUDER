import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ChatMessage {
  role: 'user' | 'assistant';
  text: string;
  timestamp: Date;
}

@Injectable({ providedIn: 'root' })
export class ChatbotService {
  constructor(private readonly http: HttpClient) {}

  sendMessage(input: string): Observable<any> {
    return this.http.post(`http://localhost:1583/ia/api/v1/execute/`, {
      template: 'CHAT',
      context_id: 'STUDER_GENERAL',
      input,
    }, {
      headers: { 'X-API-Key': 'studer-secret-key' },
    });
  }
}
