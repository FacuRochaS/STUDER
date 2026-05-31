import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { DatePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { RichTextComponent, EntityClickEvent } from '../../shared/components/rich-text/rich-text.component';
import { ManualEntityComponent, ManualEntityClickEvent } from '../../shared/components/rich-text/manual-entity/manual-entity.component';
import { UserService } from '../users/user.service';
import { User, UserUpdateRequestDTO } from '../users/user.model';
import { API_CONFIG } from '../../config/api.config';

interface TestMessage {
  id: number;
  authorUsername: string;
  authorName: string;
  content: string;
  createdAt: Date;
}

interface ImageUrls {
  original: string;
  avatar: string;
  webp: string;
  thumbnail: string;
}

@Component({
  selector: 'studer-test',
  standalone: true,
  imports: [RichTextComponent, ManualEntityComponent, DatePipe],
  templateUrl: './test.component.html',
  styleUrls: ['./test.component.css']
})
export class TestComponent {
  selectedFile: File | null = null;
  uploadSuccess = false;
  userImages: ImageUrls | null = null;

  messages: TestMessage[] = [
    {
      id: 1,
      authorUsername: 'user1',
      authorName: 'Usuario 1',
      content: 'Hola @user2, ¿viste el %bloque del curso &angular? Hay un #bug en la sección 3. Mira también el $contest1',
      createdAt: new Date()
    },
    {
      id: 2,
      authorUsername: 'user2',
      authorName: 'Usuario 2',
      content: 'Sí @user1, ya lo revisé. El problema es que &typescript no está correctamente tipado. #typescript #angular &react',
      createdAt: new Date()
    }
  ];

  constructor(private router: Router, private http: HttpClient, private userService: UserService) {}

  onFileSelected(event: any): void {
    this.selectedFile = event.target.files[0];
    this.uploadSuccess = false;
  }

  onSubmit(): void {
    if (!this.selectedFile) {
      return;
    }

    const formData = new FormData();
    formData.append('file', this.selectedFile);

    // El backend ignora los campos nulos, por lo que enviamos null para evitar errores de validación.
    const updateDto: UserUpdateRequestDTO = { email: null, password: null };
    formData.append('request', new Blob([JSON.stringify(updateDto)], { type: 'application/json' }));

    const url = `${API_CONFIG.baseUrl}${API_CONFIG.users}`;

    this.http.put(url, formData).subscribe({
      next: () => {
        this.uploadSuccess = true;
        this.selectedFile = null;
        this.fetchUserImages();
      },
      error: (err) => console.error('Error al subir la imagen:', err)
    });
  }

  fetchUserImages(): void {
    this.userService.getMe().subscribe({
      next: (user: User) => {
        if (user.profilePictureOriginalUrl) {
          this.userImages = {
            original: user.profilePictureOriginalUrl,
            avatar: user.profilePictureAvatarUrl,
            webp: user.profilePictureWebpUrl,
            thumbnail: user.profilePictureThumbnailUrl
          };
        }
      },
      error: (err) => console.error('Error al obtener las imágenes del usuario:', err)
    });
  }

  handleEntityClick(event: EntityClickEvent): void {
    console.log('Entity clicked:', event);
    // Lógica de navegación...
  }

  handleAuthorClick(event: ManualEntityClickEvent): void {
    console.log('Author clicked:', event);
    // Lógica de navegación...
  }
}
