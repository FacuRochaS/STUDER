import { Component, AfterViewInit } from '@angular/core';
import { LogoComponent } from '../../shared/components/logo/logo.component';
import { LoginRegisterComponent } from '../users/components/login-register/login-register.component';
import { PhrasesComponent } from './phrases/phrases.component';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'studer-landing',
  standalone: true,
  templateUrl: './landing.component.html',
  imports: [
    LogoComponent,
    LoginRegisterComponent,
    PhrasesComponent,
    TranslatePipe
  ],
  styleUrls: ['./landing.component.css']
})
export class LandingComponent implements AfterViewInit {
  sloganColors = ['c1', 'c2', 'c3', 'c4', 'c5', 'c6'];

  phraseKeys = [
    'landing.slogan1',
    'landing.slogan2',
    'landing.slogan3',
    'landing.slogan4',
    'landing.slogan5',
    'landing.slogan6',
  ];

  ngAfterViewInit(): void {
    this.animateSectionsOnScroll();
  }

  scrollToLogin(): void {
    const el = document.getElementById('login-section');
    el?.scrollIntoView({ behavior: 'smooth', block: 'center' });
  }

  private animateSectionsOnScroll(): void {
    const sections = Array.from(document.querySelectorAll('.landing-section'));
    const reveal = () => {
      const trigger = window.innerHeight * 0.92;
      for (const section of sections) {
        const top = (section as HTMLElement).getBoundingClientRect().top;
        if (top < trigger) {
          section.classList.add('visible');
        }
      }
    };
    window.addEventListener('scroll', reveal);
    reveal();
  }
}
