import { DOCUMENT } from '@angular/common';
import { inject, Injectable, signal } from '@angular/core';
import { TranslocoService } from '@jsverse/transloco';

import type { Locale } from '../api/models';

const LOCALE_KEY = 'books-randomized-locale';

@Injectable({ providedIn: 'root' })
export class LocaleService {
  private readonly document = inject(DOCUMENT);
  private readonly transloco = inject(TranslocoService);
  readonly locale = signal<Locale>(this.storedLocale());

  constructor() {
    this.apply(this.locale());
  }

  set(locale: Locale): void {
    localStorage.setItem(LOCALE_KEY, locale);
    this.locale.set(locale);
    this.apply(locale);
  }

  private apply(locale: Locale): void {
    this.document.documentElement.lang = locale;
    this.transloco.setActiveLang(locale);
  }

  private storedLocale(): Locale {
    return localStorage.getItem(LOCALE_KEY) === 'it' ? 'it' : 'en';
  }
}
