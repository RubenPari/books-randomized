import Component from '@glimmer/component';
import { action } from '@ember/object';
import { service } from '@ember/service';
import { on } from '@ember/modifier';
import t from '../helpers/t';
import type I18nService from '../services/i18n';

export default class SettingsPage extends Component {
  @service declare i18n: I18nService;

  get locale() {
    return this.i18n.locale;
  }

  @action
  updateLocale(event: Event) {
    this.i18n.setLocale((event.target as HTMLSelectElement).value);
  }

  <template>
    <section class="page">
      <header class="page-header">
        <h2>{{t "settings.title"}}</h2>
        <p>{{t "settings.description"}}</p>
      </header>

      <div class="card">
        <label>
          {{t "settings.language"}}
          <select value={{this.locale}} {{on "change" this.updateLocale}}>
            <option value="it">Italiano</option>
            <option value="en">English</option>
          </select>
        </label>
        <label>
          {{t "settings.preferredCategory"}}
          <input type="text" placeholder="Es. Sci-Fi" />
        </label>
      </div>
    </section>
  </template>
}
