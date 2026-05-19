import { importProvidersFrom } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { provideTranslateHttpLoader } from '@ngx-translate/http-loader';

export const provideI18n = () => [
  importProvidersFrom(
    TranslateModule.forRoot({
      fallbackLang: 'es'
    })
  ),
  provideTranslateHttpLoader({ prefix: '/assets/i18n/', suffix: '.json' })
];
