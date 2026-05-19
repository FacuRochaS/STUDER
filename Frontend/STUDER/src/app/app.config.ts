import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideI18n } from './core/i18n/i18n.provider';
import { authInterceptor } from './core/auth/auth.interceptor';
import { languageInterceptor } from './core/i18n/language.interceptor';
import { refreshInterceptor } from './core/auth/refresh.interceptor';
import { caseConverterInterceptor } from './core/http/case-converter.interceptor';
import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptors([ caseConverterInterceptor,refreshInterceptor, authInterceptor, languageInterceptor])),
    ...provideI18n()
  ]
};
