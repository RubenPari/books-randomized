import { helper } from '@ember/component/helper';
import { inject as service } from '@ember/service';
import type TranslationsService from '../services/translations';

export default class THelper extends helper {
  @service declare translations: TranslationsService;

  compute([key]: [string]) {
    return this.translations.t(key);
  }
}
