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
    { label: 'sidebar.home', icon: 'pi pi-home', route: '/home', colorClass: 'nav--home' },
    { label: 'sidebar.courses', icon: 'pi pi-book', route: '/courses', colorClass: 'nav--courses' },
    { label: 'sidebar.calendar', icon: 'pi pi-calendar', route: '/calendar', colorClass: 'nav--calendar' },
    { label: 'sidebar.discussions', icon: 'pi pi-megaphone', route: '/discussions', colorClass: 'nav--discussions' },
    { label: 'sidebar.messages', icon: 'pi pi-comments', route: '/messages', colorClass: 'nav--messages' },
    { label: 'sidebar.account', icon: 'pi pi-user', route: '/account', colorClass: 'nav--account' },
  ];
}

