import Service from '@ember/service';
import { tracked } from '@glimmer/tracking';

/**
 * Internationalization service managing the active locale.
 * Persists the user's choice in localStorage; defaults to Italian ('it').
 */
export default class I18nService extends Service {
  @tracked locale = 'it';

  constructor() {
    super();
    this.locale = window.localStorage.getItem('locale') || 'it';
  }

  setLocale(locale: string) {
    this.locale = locale;
    window.localStorage.setItem('locale', locale);
  }
}
