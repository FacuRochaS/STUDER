import { Injectable, OnDestroy } from '@angular/core';
import { BehaviorSubject, Subject } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';

export type Language = 'en' | 'es';

@Injectable({ providedIn: 'root' })
export class LanguageService implements OnDestroy {
  private readonly key = 'preferred_language';
  private readonly subject = new BehaviorSubject<Language>('es');
  private readonly destroy$ = new Subject<void>();

  constructor(private translate: TranslateService) {
    this.translate.addLangs(['es', 'en']);
    this.initializeLanguage();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Inicializa el idioma de forma síncrona en el constructor
   */
  private initializeLanguage(): void {
    const saved = localStorage.getItem(this.key);
    let lang: Language;

    if (saved === 'en' || saved === 'es') {
      lang = saved;
    } else {
      const browser = (navigator.language ?? '').toLowerCase();
      lang = browser.startsWith('en') ? 'en' : 'es';
    }

    this.subject.next(lang);
    this.translate.use(lang).subscribe();
  }

  init(): void {
    // Ya inicializado en el constructor
  }

  get current(): Language {
    return this.subject.value;
  }

  setLanguage(lang: Language): void {
    this.subject.next(lang);
    localStorage.setItem(this.key, lang);
    document.documentElement.setAttribute('lang', lang);

    this.translate.use(lang).subscribe();
  }
}
