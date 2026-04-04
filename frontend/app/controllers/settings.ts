import Controller from '@ember/controller';
import { action } from '@ember/object';
import { inject as service } from '@ember/service';
import type I18nService from '../services/i18n';

/** Controller for the settings page. Exposes the current locale and an action to change it. */
export default class SettingsController extends Controller {
  @service declare i18n: I18nService;

  get locale() {
    return this.i18n.locale;
  }

  @action
  updateLocale(event: Event) {
    this.i18n.setLocale((event.target as HTMLSelectElement).value);
  }
}
