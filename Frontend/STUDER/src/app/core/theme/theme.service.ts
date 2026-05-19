import { Injectable } from '@angular/core';

export type Theme = 'light' | 'dark';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly themeKey = 'preferred_theme';

  setTheme(theme: Theme) {
    document.body.classList.remove('theme-light', 'theme-dark');
    document.body.classList.add(`theme-${theme}`);
    localStorage.setItem(this.themeKey, theme);
  }

  loadTheme() {
    const theme = (localStorage.getItem(this.themeKey) as Theme) || 'light';
    this.setTheme(theme);
  }

  get current(): Theme {
    return (localStorage.getItem(this.themeKey) as Theme) || 'light';
  }
}

