import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';

interface NavItem {
  label: string;
  icon: string;
  route: string;
  colorClass: string;
}

@Component({
  selector: 'studer-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, TranslatePipe],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent {
  readonly navItems: NavItem[] = [
    { label: 'sidebar.home', icon: 'fa-solid fa-house', route: '/home', colorClass: 'nav--home' },
    { label: 'sidebar.contest', icon: 'fa-solid fa-list-check', route: '/contest', colorClass: 'nav--contest' },
    { label: 'sidebar.courses', icon: 'fa-solid fa-book', route: '/courses', colorClass: 'nav--courses' },
    { label: 'sidebar.discussions', icon: 'fa-solid fa-comments', route: '/discussions', colorClass: 'nav--discussions' },
    { label: 'sidebar.messages', icon: 'fa-solid fa-envelope', route: '/messages', colorClass: 'nav--messages' },
    { label: 'sidebar.account', icon: 'fa-solid fa-user', route: '/account', colorClass: 'nav--account' },
  ];
}
