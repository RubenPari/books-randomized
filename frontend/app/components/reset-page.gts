import Component from '@glimmer/component';
import { tracked } from '@glimmer/tracking';
import { action } from '@ember/object';
import { service } from '@ember/service';
import { on } from '@ember/modifier';
import t from '../helpers/t';
import type ApiService from '../services/api';
import type TranslationsService from '../services/translations';

export default class ResetPage extends Component {
  @service declare api: ApiService;
  @service declare translations: TranslationsService;

  @tracked email = '';
  @tracked message: string | null = null;
  @tracked error: string | null = null;

  @action updateEmail(event: Event) {
    this.email = (event.target as HTMLInputElement).value;
  }

  @action
  async submit() {
    this.error = null;
    this.message = null;
    try {
      await this.api.requestPasswordReset(this.email);
      this.message = this.translations.t('auth.resetTokenSent');
    } catch (error) {
      this.error =
        error instanceof Error
          ? error.message
          : this.translations.t('auth.resetSendFailed');
    }
  }

  <template>
    <section class="page narrow">
      <header class="page-header">
        <h2>{{t "auth.resetTitle"}}</h2>
        <p>{{t "auth.resetDescription"}}</p>
      </header>

      <div class="card">
        <label>
          {{t "auth.email"}}
          <input
            type="email"
            value={{this.email}}
            {{on "input" this.updateEmail}}
          />
        </label>
        {{#if this.message}}
          <p class="success">{{this.message}}</p>
        {{/if}}
        {{#if this.error}}
          <p class="error">{{this.error}}</p>
        {{/if}}
        <button type="button" class="primary" {{on "click" this.submit}}>{{t
            "auth.resetSubmit"
          }}</button>
      </div>
    </section>
  </template>
}
