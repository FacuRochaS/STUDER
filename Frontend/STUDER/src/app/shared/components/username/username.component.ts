import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'studer-username',
  standalone: true,
  imports: [RouterLink],
  template: `<a class="username" [routerLink]="'/user/' + username">{{'@' + username}}</a>`,
  styles: [`
    .username {
      color: var(--color-primary);
      font-weight: 600;
      text-decoration: none;
      cursor: pointer;
      transition: opacity 0.2s;
      font-family: 'Roboto', Arial, Helvetica, sans-serif;
    }
    .username:hover {
      opacity: 0.75;
      text-decoration: underline;
    }
  `]
})
export class UsernameComponent {
  @Input({ required: true }) username!: string;
}

