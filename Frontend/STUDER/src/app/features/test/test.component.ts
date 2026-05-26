import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { DatePipe } from '@angular/common';
import { RichTextComponent, EntityClickEvent } from '../../shared/components/rich-text/rich-text.component';
import { ManualEntityComponent, ManualEntityClickEvent } from '../../shared/components/rich-text/manual-entity/manual-entity.component';

interface TestMessage {
  id: number;
  authorUsername: string;
  authorName: string;
  content: string;
  createdAt: Date;
}

@Component({
  selector: 'studer-test',
  standalone: true,
  imports: [RichTextComponent, ManualEntityComponent, DatePipe],
  templateUrl: './test.component.html',
  styleUrls: ['./test.component.css']
})
export class TestComponent {
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
    },
    {
      id: 3,
      authorUsername: 'user3',
      authorName: 'Usuario 3',
      content: 'Chicos, @user1 tiene razón. En el %bloque-intro del curso &javascript encontré un error. Denle una mirada en $contest2. #frontend &vue %modulo-avanzado',
      createdAt: new Date()
    },
    {
      id: 4,
      authorUsername: 'user1',
      authorName: 'Usuario 1',
      content: 'Excelente punto @user3. He actualizado &python y ahora funciona mejor. Gracias por revisar el $contest3 y los %bloques. #solved #python',
      createdAt: new Date()
    }
  ];

  constructor(private router: Router) {}

  handleEntityClick(event: EntityClickEvent): void {
    console.log('Entity clicked:', event);
    switch (event.type) {
      case 'user':
        this.router.navigate(['/user', `@${event.value}`]);
        break;
      case 'tag':
        this.router.navigate(['/tags', event.value]);
        break;
      case 'course':
        this.router.navigate(['/courses', event.value]);
        break;
      case 'contest':
        this.router.navigate(['/contests', event.value]);
        break;
      case 'block':
        this.router.navigate(['/blocks', event.value]);
        break;
    }
  }

  handleAuthorClick(event: ManualEntityClickEvent): void {
    console.log('Author clicked:', event);
    this.router.navigate(['/user', `@${event.value}`]);
  }
}

