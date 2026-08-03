import { Component, AfterViewInit, HostListener, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { LogoComponent } from '../../shared/components/logo/logo.component';
import { LoginRegisterComponent } from '../users/components/login-register/login-register.component';
import { PhrasesComponent } from './phrases/phrases.component';
import { ThemeService } from '../../core/theme/theme.service';
import { LanguageService, Language } from '../../core/i18n/language.service';
import { TranslatePipe } from '@ngx-translate/core';
import { ChatbotComponent } from '../chatbot/chatbot.component';

@Component({
  selector: 'studer-landing',
  standalone: true,
  templateUrl: './landing.component.html',
  imports: [
    LogoComponent,
    LoginRegisterComponent,
    PhrasesComponent,
    TranslatePipe,
    RouterModule,
    ChatbotComponent,
  ],
  styleUrls: ['./landing.component.css']
})
export class LandingComponent implements AfterViewInit {
  private themeService = inject(ThemeService);
  private languageService = inject(LanguageService);

  sloganColors = ['c1', 'c2', 'c3', 'c4', 'c5', 'c6'];
  navScrolled = false;

  phraseKeys = [
    'landing.slogan1',
    'landing.slogan2',
    'landing.slogan3',
    'landing.slogan4',
    'landing.slogan5',
    'landing.slogan6',
  ];

  @HostListener('window:scroll')
  onWindowScroll(): void {
    this.navScrolled = window.scrollY > 20;
  }

  ngAfterViewInit(): void {
    this.animateSectionsOnScroll();
  }

  scrollToLogin(): void {
    const el = document.getElementById('login-section');
    el?.scrollIntoView({ behavior: 'smooth', block: 'center' });
  }

  toggleTheme(event: MouseEvent): void {
    const next = this.themeService.current === 'light' ? 'dark' : 'light';
    this.themeService.setTheme(next, { animate: true, event });
  }

  get themeIcon(): string {
    return this.themeService.current === 'light' ? 'pi pi-moon' : 'pi pi-sun';
  }

  toggleLanguage(): void {
    const next: Language = this.languageService.current === 'es' ? 'en' : 'es';
    this.languageService.setLanguage(next);
  }

  get langLabel(): string {
    return this.languageService.current.toUpperCase();
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
