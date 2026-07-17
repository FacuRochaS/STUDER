import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_CONFIG } from '../../config/api.config';

export interface UploadResponseDTO {
  url: string;
  filename: string;
  size: number;
  mimeType: string;
}

@Injectable({ providedIn: 'root' })
export class UploadService {
  private readonly http = inject(HttpClient);

  uploadImage(file: File, folder?: string): Observable<UploadResponseDTO> {
    const formData = new FormData();
    formData.append('file', file, file.name);
    if (folder) formData.append('folder', folder);
    return this.http.post<UploadResponseDTO>(`${API_CONFIG.baseUrl}${API_CONFIG.upload}/image`, formData);
  }
}
