import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

export type Theme = 'light' | 'dark';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private readonly themeKey = 'app-theme';

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {}

  /**
   * Cambia el tema de la aplicación expandiendo un círculo desde el origen del clic.
   */
  setTheme(theme: Theme, options?: { animate?: boolean; event?: MouseEvent }) {
    if (!isPlatformBrowser(this.platformId)) return;

    const animate = options?.animate ?? true;
    const event = options?.event;

    if (this.current === theme && animate) return;

    // Si no se requiere animación o el navegador no la soporta
    if (!animate || !(document as any).startViewTransition) {
      this.applyThemeDOM(theme);
      return;
    }

    // 1. Obtener las coordenadas del clic. Si no hay evento, usamos el centro de la pantalla.
    const x = event ? event.clientX : window.innerWidth / 2;
    const y = event ? event.clientY : window.innerHeight / 2;

    // 2. Calcular la distancia desde el clic hasta la esquina más lejana de la pantalla (Radio final)
    const endRadius = Math.hypot(
      Math.max(x, window.innerWidth - x),
      Math.max(y, window.innerHeight - y)
    );

    // 3. Iniciar la transición de vista
    const transition = (document as any).startViewTransition(() => {
      this.applyThemeDOM(theme);
    });

    // 4. Cuando la transición esté lista, animamos el pseudo-elemento "new" (el nuevo tema)
    transition.ready.then(() => {
      document.documentElement.animate(
        {
          // Definimos el camino del clip-path: de un círculo de 0px a uno que cubre la pantalla
          clipPath: [
            `circle(0px at ${x}px ${y}px)`,
            `circle(${endRadius}px at ${x}px ${y}px)`
          ],
        },
        {
          duration: 500,
          easing: 'cubic-bezier(0.4, 0, 0.2, 1)',
          // Animamos la capa nueva para que se expanda sobre la vieja
          pseudoElement: '::view-transition-new(root)',
        }
      );
    });
  }

  private applyThemeDOM(theme: Theme) {
    document.body.classList.remove('theme-light', 'theme-dark');
    document.body.classList.add(`theme-${theme}`);
    localStorage.setItem(this.themeKey, theme);
  }

  loadTheme() {
    if (!isPlatformBrowser(this.platformId)) return;
    const theme = (localStorage.getItem(this.themeKey) as Theme) || 'light';
    this.setTheme(theme, { animate: false });
  }

  get current(): Theme {
    if (!isPlatformBrowser(this.platformId)) return 'light';
    return (localStorage.getItem(this.themeKey) as Theme) || 'light';
  }
}
