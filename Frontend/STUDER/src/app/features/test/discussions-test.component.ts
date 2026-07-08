import { Component, Injectable } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Observable, of } from 'rxjs';
import { DiscussionComponent } from '../discussions/components/discussion.component';
import { DiscussionService } from '../discussions/discussion.service';
import { DiscussionPageResponseDTO, DiscussionResponseDTO, DiscussionMessagePageResponseDTO } from '../discussions/discussion.model';

// --- 1. DATOS MOCKEADOS ---
const MOCK_DISCUSSIONS: DiscussionResponseDTO[] = [
  {
    id: 1,
    title: '¿Cuál es el mejor framework de Frontend en 2024?',
    description: 'Abro debate sobre las ventajas y desventajas de Angular, React, Vue, Svelte y otros. ¿Qué opinan? Quiero argumentos, no una guerra de fans.',
    owner: {
      id: 1,
      username: 'facundo',
      firstName: 'Facundo',
      lastName: 'García',
      profilePictureWebpUrl: 'https://i.pravatar.cc/150?u=facundo',
      profilePictureOriginalUrl: 'null',
      profilePictureAvatarUrl: 'null',
      profilePictureThumbnailUrl: 'null'
    },
    tags: ['frontend', 'angular', 'react', 'webdev'],
    createdAt: new Date().toISOString(),
    favourite: true,
    messageCount: 2,
    likeCount: 15,
    favouriteCount: 3,
    participationType: 'OWNER'
  },
  {
    id: 2,
    title: 'Inteligencia Artificial y el futuro del trabajo',
    description: '¿Cómo creen que la IA afectará al mercado laboral en los próximos 5 años? ¿Qué habilidades serán más importantes?',
    owner: {
      id: 2,
      username: 'elonMusk',
      firstName: 'Elon',
      lastName: 'Musk',
      profilePictureWebpUrl: 'https://i.pravatar.cc/150?u=elon',
      profilePictureOriginalUrl: 'null',
      profilePictureAvatarUrl: 'null',
      profilePictureThumbnailUrl: 'null'
    },
    tags: ['ia', 'futuro', 'tecnologia'],
    createdAt: new Date(Date.now() - 86400000 * 2).toISOString(), // Hace 2 días
    favourite: false,
    messageCount: 0,
    likeCount: 42,
    favouriteCount: 12,
    participationType: 'NONE'
  },
];

const MOCK_MESSAGES: Record<string, any[]> = {
  '1': [ // Mensajes para la discusión con ID 1
    {
      id: 101,
      sender: {
        id: 3,
        username: 'reactLover',
        firstName: 'React',
        lastName: 'Lover',
        profilePictureWebpUrl: 'https://i.pravatar.cc/150?u=react',
        profilePictureOriginalUrl: null,
        profilePictureAvatarUrl: null,
        profilePictureThumbnailUrl: null
      },
      content: '<p>Obviamente <strong>React</strong>. La flexibilidad que te da con el ecosistema es inigualable.</p>',
      imageRef: null,
      createdAt: new Date(Date.now() - 3600000).toISOString(), // Hace 1 hora
      likeCount: 5,
      likedByCurrentUser: false,
      children: []
    },
    {
      id: 102,
      sender: {
        id: 4,
        username: 'svelteFan',
        firstName: 'Svelte',
        lastName: 'Fan',
        profilePictureWebpUrl: 'https://i.pravatar.cc/150?u=svelte',
        profilePictureOriginalUrl: null,
        profilePictureAvatarUrl: null,
        profilePictureThumbnailUrl: null
      },
      content: '<p>Están durmiendo en Svelte. Menos boilerplate, más reactividad nativa. Es el futuro.</p>',
      imageRef: 'https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExajBqM2hkaHh6N25zY2E0dGg2N2N2eDB6c2U4dDR0dGZqZzRjM25zZSZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/13HgwGsXF0aiGY/giphy.gif',
      createdAt: new Date().toISOString(),
      likeCount: 10,
      likedByCurrentUser: true,
      children: []
    }
  ]
};


// --- 2. SERVICIO MOCKEADO ---
@Injectable()
export class MockDiscussionService extends DiscussionService {
  constructor() { super(null!); } // Pasamos null porque no usaremos el http real

  override getMyOwnDiscussions(page = 0): Observable<DiscussionPageResponseDTO> {
    return of({ discussions: MOCK_DISCUSSIONS, totalElements: 2, hasMore: false, currentPage: 0 });
  }
  override getMyFavouriteDiscussions(page = 0): Observable<DiscussionPageResponseDTO> {
    return of({ discussions: MOCK_DISCUSSIONS.filter(d => d.favourite), totalElements: 1, hasMore: false, currentPage: 0 });
  }
  override getMyDiscussions(page = 0): Observable<DiscussionPageResponseDTO> {
    return of({ discussions: MOCK_DISCUSSIONS, totalElements: 2, hasMore: false, currentPage: 0 });
  }
  override getPopularDiscussions(page = 0): Observable<DiscussionPageResponseDTO> {
    return of({ discussions: MOCK_DISCUSSIONS, totalElements: 2, hasMore: false, currentPage: 0 });
  }
  override getNewDiscussions(page = 0): Observable<DiscussionPageResponseDTO> {
    return of({ discussions: MOCK_DISCUSSIONS, totalElements: 2, hasMore: false, currentPage: 0 });
  }
  override getPublicDiscussions(page = 0): Observable<DiscussionPageResponseDTO> {
    return of({ discussions: MOCK_DISCUSSIONS, totalElements: 2, hasMore: false, currentPage: 0 });
  }

  override getMessages(discussionId: number, page = 0): Observable<DiscussionMessagePageResponseDTO> {
    const messages = MOCK_MESSAGES[discussionId] || [];
    return of({ messages, totalElements: messages.length, hasMore: false, currentPage: 0 });
  }
}


// --- 3. COMPONENTE DE PRUEBA ---
@Component({
  selector: 'app-discussions-test',
  standalone: true,
  imports: [CommonModule, DiscussionComponent],
  templateUrl: './discussions-test.component.html',
  // Aquí está la magia: proveemos el servicio mockeado
  providers: [{ provide: DiscussionService, useClass: MockDiscussionService }]
})
export class DiscussionsTestComponent { }
