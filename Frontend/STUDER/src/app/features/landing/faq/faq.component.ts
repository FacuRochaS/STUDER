import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { ThemeService } from '../../../core/theme/theme.service';
import { LanguageService, Language } from '../../../core/i18n/language.service';
import { LogoComponent } from '../../../shared/components/logo/logo.component';
import {ChatbotComponent} from '../../chatbot/chatbot.component';


@Component({
  selector: 'studer-faq',
  standalone: true,
  imports: [CommonModule, RouterModule, TranslateModule, LogoComponent, ChatbotComponent],
  template: `
    <div class="faq-root">
      <nav class="faq-nav">
        <a routerLink="/" class="faq-nav__logo"><studer-logo [size]="1.3" animation="pulse"></studer-logo></a>
        <div class="faq-nav__actions">
          <button class="faq-nav__btn" (click)="toggleTheme($event)"><i [class]="themeIcon"></i></button>
          <button class="faq-nav__btn faq-nav__lang" (click)="toggleLanguage()">{{ langLabel }}</button>
          <a routerLink="/" class="faq-nav__back">{{ 'common.back' | translate }}</a>
        </div>
      </nav>

      <div class="faq-hero">
        <h1>{{ 'faq.title' | translate }}</h1>
        <p>{{ 'faq.subtitle' | translate }}</p>
      </div>

      <div class="faq-tabs">
        <button [class.active]="activeTab === 'faq'" (click)="activeTab = 'faq'">{{ 'faq.tab_title' | translate }}</button>
        <button [class.active]="activeTab === 'terms'" (click)="activeTab = 'terms'">{{ 'terms.tab_title' | translate }}</button>
      </div>

      <main class="faq-body">
        @if (activeTab === 'faq') {
          <div class="faq-list">
            @for (item of faqItems; track item.key; let i = $index) {
              <details class="faq-item" [open]="i === 0">
                <summary class="faq-item__q">
                  <span>{{ item.title | translate }}</span>
                  <i class="pi pi-chevron-down faq-item__arrow"></i>
                </summary>
                <div class="faq-item__a">
                  <p>{{ item.answer | translate }}</p>
                </div>
              </details>
            }
          </div>
        }

        @if (activeTab === 'terms') {
          <div class="terms-content">
            <p class="terms-date">{{ 'terms.last_updated' | translate }}</p>
            <h3>1. {{ 'terms.acceptance_title' | translate }}</h3>
            <p>{{ 'terms.acceptance_text' | translate }}</p>
            <h3>2. {{ 'terms.age_title' | translate }}</h3>
            <p>{{ 'terms.age_text' | translate }}</p>
            <h3>3. {{ 'terms.account_title' | translate }}</h3>
            <p>{{ 'terms.account_text' | translate }}</p>
            <h3>4. {{ 'terms.conduct_title' | translate }}</h3>
            <p>{{ 'terms.conduct_text' | translate }}</p>
            <h3>5. {{ 'terms.content_title' | translate }}</h3>
            <p>{{ 'terms.content_text' | translate }}</p>
            <h3>6. {{ 'terms.intellectual_title' | translate }}</h3>
            <p>{{ 'terms.intellectual_text' | translate }}</p>
            <h3>7. {{ 'terms.privacy_title' | translate }}</h3>
            <p>{{ 'terms.privacy_text' | translate }}</p>
            <h3>8. {{ 'terms.data_deletion_title' | translate }}</h3>
            <p>{{ 'terms.data_deletion_text' | translate }}</p>
            <h3>9. {{ 'terms.liability_title' | translate }}</h3>
            <p>{{ 'terms.liability_text' | translate }}</p>
            <h3>10. {{ 'terms.modifications_title' | translate }}</h3>
            <p>{{ 'terms.modifications_text' | translate }}</p>
            <h3>11. {{ 'terms.contact_title' | translate }}</h3>
            <p>{{ 'terms.contact_text' | translate }}</p>
          </div>
        }
      </main>

      <footer class="faq-footer">
        <span>&copy; 2026 Studer</span>
      </footer>

      <studer-chatbot></studer-chatbot>
    </div>
  `,
  styles: [`
    :host { display: block; min-height: 100vh; background: var(--layout-body-bg, #f7f7f9); color: var(--color-text-prim); }

    .faq-nav { display: flex; align-items: center; justify-content: space-between; padding: 0.75rem 2rem; background: var(--color-bg); box-shadow: 0 1px 3px var(--box-shadow-color); position: sticky; top: 0; z-index: 10; }
    .faq-nav__logo { display: flex; text-decoration: none; }
    .faq-nav__actions { display: flex; align-items: center; gap: 0.5rem; }
    .faq-nav__btn { background: none; border: none; cursor: pointer; color: var(--color-text-secu); font-size: 1.05rem; padding: 0.35rem; border-radius: 6px; width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; transition: background 0.15s; }
    .faq-nav__btn:hover { background: rgba(128,128,128,0.1); }
    .faq-nav__lang { font-size: 0.75rem; font-weight: 700; letter-spacing: 0.04em; width: auto; padding: 0.3rem 0.5rem; }
    .faq-nav__back { padding: 0.4rem 0.9rem; border-radius: 6px; background: var(--color-primary); color: #fff; text-decoration: none; font-size: 0.82rem; font-weight: 600; transition: opacity 0.2s; }
    .faq-nav__back:hover { opacity: 0.85; }

    .faq-hero { text-align: center; padding: 3rem 1.5rem 2rem; }
    .faq-hero h1 { font-size: 2rem; margin: 0 0 0.75rem; color: var(--color-text-prim); }
    .faq-hero p { font-size: 1rem; color: var(--color-text-secu); max-width: 600px; margin: 0 auto; line-height: 1.5; }

    .faq-tabs { display: flex; justify-content: center; gap: 0; border-bottom: 2px solid var(--border); margin: 0 1.5rem; }
    .faq-tabs button { padding: 0.6rem 1.8rem; border: none; background: none; font-size: 0.95rem; font-weight: 600; color: var(--color-text-secu); cursor: pointer; border-bottom: 2px solid transparent; margin-bottom: -2px; transition: color 0.2s, border-color 0.2s; font-family: inherit; }
    .faq-tabs button:hover { color: var(--color-text-prim); }
    .faq-tabs button.active { color: var(--color-primary); border-bottom-color: var(--color-primary); }

    .faq-body { max-width: 760px; margin: 0 auto; padding: 2rem 1.5rem 3rem; }

    .faq-list { display: flex; flex-direction: column; gap: 0.5rem; }
    .faq-item { border: 1px solid var(--border); border-radius: 10px; overflow: hidden; background: var(--color-bg); box-shadow: 0 1px 2px var(--box-shadow-color); transition: box-shadow 0.2s; }
    .faq-item[open] { box-shadow: 0 2px 10px var(--box-shadow-color); }
    .faq-item__q { padding: 1rem 1.25rem; cursor: pointer; font-weight: 600; font-size: 0.95rem; display: flex; justify-content: space-between; align-items: center; user-select: none; color: var(--color-text-prim); }
    .faq-item__q:hover { color: var(--color-primary); }
    .faq-item__arrow { transition: transform 0.2s; font-size: 0.75rem; color: var(--color-text-secu); }
    .faq-item[open] .faq-item__arrow { transform: rotate(180deg); }
    .faq-item__a { padding: 0 1.25rem 1.1rem; color: var(--color-text-secu); line-height: 1.65; font-size: 0.9rem; }
    .faq-item__a p { margin: 0; }

    .terms-content { font-size: 0.9rem; line-height: 1.7; color: var(--color-text-secu); }
    .terms-content h3 { font-size: 1rem; color: var(--color-text-prim); margin: 1.5rem 0 0.5rem; }
    .terms-content p { margin: 0 0 0.75rem; }
    .terms-date { font-style: italic; opacity: 0.6; margin-bottom: 1.5rem; }

    .faq-footer { text-align: center; padding: 1.5rem; border-top: 1px solid var(--border); font-size: 0.8rem; color: var(--color-text-secu); }
  `]
})
export class FaqComponent {
  private themeService = inject(ThemeService);
  private languageService = inject(LanguageService);

  activeTab: 'faq' | 'terms' = 'faq';

  faqItems = [
    { key: 'what_is', title: 'faq.what_is_q', answer: 'faq.what_is_a' },
    { key: 'free', title: 'faq.free_q', answer: 'faq.free_a' },
    { key: 'age', title: 'faq.age_q', answer: 'faq.age_a' },
    { key: 'blocks', title: 'faq.blocks_q', answer: 'faq.blocks_a' },
    { key: 'courses', title: 'faq.courses_q', answer: 'faq.courses_a' },
    { key: 'contests', title: 'faq.contests_q', answer: 'faq.contests_a' },
    { key: 'moderation', title: 'faq.moderation_q', answer: 'faq.moderation_a' },
    { key: 'data', title: 'faq.data_q', answer: 'faq.data_a' },
    { key: 'delete', title: 'faq.delete_q', answer: 'faq.delete_a' },
  ];

  toggleTheme(event: MouseEvent): void {
    const next = this.themeService.current === 'light' ? 'dark' : 'light';
    this.themeService.setTheme(next, { animate: true, event });
  }
  get themeIcon(): string { return this.themeService.current === 'light' ? 'pi pi-moon' : 'pi pi-sun'; }
  toggleLanguage(): void {
    const next: Language = this.languageService.current === 'es' ? 'en' : 'es';
    this.languageService.setLanguage(next);
  }
  get langLabel(): string { return this.languageService.current.toUpperCase(); }
}
