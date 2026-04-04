import Helper from '@ember/component/helper';
import { service } from '@ember/service';
import type TranslationsService from '../services/translations';

/** Template helper that delegates to the TranslationsService to resolve a translation key. */
export default class THelper extends Helper {
  @service declare translations: TranslationsService;

  compute([key]: [string]) {
    return this.translations.t(key);
  }
}
