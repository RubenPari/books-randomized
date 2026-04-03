import Service from '@ember/service';
import { tracked } from '@glimmer/tracking';

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
