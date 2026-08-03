import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { ThemeService } from '../../../core/theme/theme.service';
import { LanguageService, Language } from '../../../core/i18n/language.service';
import { LogoComponent } from '../../../shared/components/logo/logo.component';
import { ChatbotComponent } from '../../chatbot/chatbot.component';

@Component({
  selector: 'studer-faq',
  standalone: true,
  imports: [CommonModule, RouterModule, TranslateModule, LogoComponent, ChatbotComponent],
  template: `
    <div class="faq">
      <nav class="faq__nav">
        <a routerLink="/" class="faq__logo"><studer-logo [size]="1.4" animation="pulse"></studer-logo></a>
        <div class="faq__actions">
          <button class="faq__btn" (click)="toggleTheme($event)"><i [class]="themeIcon"></i></button>
          <button class="faq__btn faq__lang" (click)="toggleLanguage()">{{ langLabel }}</button>
          <a routerLink="/" class="faq__back"><i class="pi pi-arrow-left"></i> {{ 'common.back' | translate }}</a>
        </div>
      </nav>

      <div class="faq__hero">
        <h1>{{ 'faq.title' | translate }}</h1>
        <p>{{ 'faq.subtitle' | translate }}</p>
      </div>

      <div class="faq__tabs">
        <button class="faq__tab" [class.active]="activeTab === 'faq'" (click)="activeTab = 'faq'">{{ 'faq.tab_title' | translate }}</button>
        <button class="faq__tab" [class.active]="activeTab === 'terms'" (click)="activeTab = 'terms'">{{ 'terms.tab_title' | translate }}</button>
      </div>

      <main class="faq__body">
        @if (activeTab === 'faq') {
          <div class="faq__list">
            @for (item of faqItems; track item.key) {
              <details class="faq__item">
                <summary class="faq__q">
                  <span>{{ item.title | translate }}</span>
                  <i class="pi pi-chevron-down faq__arrow"></i>
                </summary>
                <div class="faq__a">
                  <p>{{ item.answer | translate }}</p>
                </div>
              </details>
            }
          </div>
        }

        @if (activeTab === 'terms') {
          <div class="faq__terms">
            <p class="faq__terms-date">{{ 'terms.last_updated' | translate }}</p>
            @for (section of termsSections; track section.title) {
              <h3>{{ section.title | translate }}</h3>
              <p>{{ section.text | translate }}</p>
            }
          </div>
        }
      </main>

      <studer-chatbot></studer-chatbot>
    </div>
  `,
  styles: [`
    :host { display: block; min-height: 100vh; background: var(--layout-body-bg, #f0f0f3); color: var(--color-text-prim); }

    .faq__nav { display: flex; align-items: center; justify-content: space-between; padding: 0.6rem 1.25rem; background: var(--color-bg); border-bottom: 1px solid var(--border); position: sticky; top: 0; z-index: 10; }
    .faq__logo { display: flex; text-decoration: none; }
    .faq__actions { display: flex; align-items: center; gap: 0.4rem; }
    .faq__btn { background: none; border: none; cursor: pointer; color: var(--color-text-secu); font-size: 1rem; padding: 0.4rem; border-radius: 8px; width: 34px; height: 34px; display: flex; align-items: center; justify-content: center; transition: background 0.15s; }
    .faq__btn:hover { background: var(--sidebar-hover-bg); }
    .faq__lang { font-size: 0.72rem; font-weight: 700; letter-spacing: 0.04em; width: auto; padding: 0.3rem 0.5rem; }
    .faq__back { display: inline-flex; align-items: center; gap: 0.35rem; padding: 0.5rem 1rem; border-radius: 10px; background: var(--color-primary); color: var(--color-text-btn); text-decoration: none; font-size: 0.84rem; font-weight: 600; transition: opacity 0.2s; }
    .faq__back:hover { opacity: 0.9; }

    .faq__hero { text-align: center; padding: 2.5rem 1.5rem 1.5rem; }
    .faq__hero h1 { font-size: 1.5rem; margin: 0 0 0.5rem; color: var(--color-text-prim); }
    .faq__hero p { font-size: 0.9rem; color: var(--color-text-secu); max-width: 500px; margin: 0 auto; line-height: 1.5; }

    /* Pill Tabs */
    .faq__tabs { display: flex; justify-content: center; gap: 0.2rem; padding: 0.35rem; background: var(--color-bg); border-radius: 10px; width: fit-content; margin: 0 auto 1.5rem; box-shadow: 0 1px 3px rgba(0,0,0,0.04); }
    .faq__tab { padding: 0.5rem 1.2rem; border: none; background: transparent; color: var(--color-text-secu); font-size: 0.82rem; font-weight: 600; cursor: pointer; border-radius: 8px; font-family: inherit; transition: background 0.2s, color 0.2s; }
    .faq__tab:hover { color: var(--color-text-prim); }
    .faq__tab.active { background: var(--input-background); color: var(--color-primary); }

    .faq__body { max-width: 720px; margin: 0 auto; padding: 0 1.5rem 3rem; }

    .faq__list { display: flex; flex-direction: column; gap: 0.4rem; }
    .faq__item { background: var(--color-bg); border-radius: 12px; overflow: hidden; box-shadow: 0 1px 3px rgba(0,0,0,0.04); transition: box-shadow 0.2s; }
    .faq__item[open] { box-shadow: 0 2px 10px rgba(0,0,0,0.06); }
    .faq__q { padding: 0.9rem 1.1rem; cursor: pointer; font-weight: 600; font-size: 0.9rem; display: flex; justify-content: space-between; align-items: center; user-select: none; color: var(--color-text-prim); }
    .faq__q:hover { color: var(--color-primary); }
    .faq__arrow { transition: transform 0.2s; font-size: 0.7rem; color: var(--color-text-secu); flex-shrink: 0; }
    .faq__item[open] .faq__arrow { transform: rotate(180deg); }
    .faq__a { padding: 0 1.1rem 1rem; color: var(--color-text-secu); line-height: 1.6; font-size: 0.88rem; }
    .faq__a p { margin: 0; }

    .faq__terms { font-size: 0.88rem; line-height: 1.7; color: var(--color-text-secu); }
    .faq__terms h3 { font-size: 0.95rem; color: var(--color-text-prim); margin: 1.5rem 0 0.4rem; }
    .faq__terms p { margin: 0 0 0.6rem; }
    .faq__terms-date { opacity: 0.5; font-size: 0.8rem; margin-bottom: 1rem; }
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

  termsSections = [
    { title: 'terms.acceptance_title', text: 'terms.acceptance_text' },
    { title: 'terms.age_title', text: 'terms.age_text' },
    { title: 'terms.account_title', text: 'terms.account_text' },
    { title: 'terms.conduct_title', text: 'terms.conduct_text' },
    { title: 'terms.content_title', text: 'terms.content_text' },
    { title: 'terms.intellectual_title', text: 'terms.intellectual_text' },
    { title: 'terms.privacy_title', text: 'terms.privacy_text' },
    { title: 'terms.data_deletion_title', text: 'terms.data_deletion_text' },
    { title: 'terms.liability_title', text: 'terms.liability_text' },
    { title: 'terms.modifications_title', text: 'terms.modifications_text' },
    { title: 'terms.contact_title', text: 'terms.contact_text' },
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
