import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { ApplicationConfig, Injectable, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideTransloco, TranslocoLoader } from '@jsverse/transloco';
import { of } from 'rxjs';
import type { Translation } from '@jsverse/transloco';
import type { Observable } from 'rxjs';

import { authInterceptor } from './core/auth/auth.interceptor';
import { EN, IT } from './core/i18n/catalogs';
import { routes } from './app.routes';

@Injectable({ providedIn: 'root' })
class InlineTranslationLoader implements TranslocoLoader {
  getTranslation(language: string): Observable<Translation> {
    return of(language === 'it' ? IT : EN);
  }
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])),
    provideTransloco({
      config: {
        availableLangs: ['en', 'it'],
        defaultLang: 'en',
        fallbackLang: 'en',
        reRenderOnLangChange: true,
        prodMode: true,
      },
      loader: InlineTranslationLoader,
    }),
  ],
};
