import Component from '@glimmer/component';
import { tracked } from '@glimmer/tracking';
import { action } from '@ember/object';
import { service } from '@ember/service';
import { on } from '@ember/modifier';
import t from '../helpers/t';
import type ApiService from '../services/api';

export default class RegisterPage extends Component {
  @service declare api: ApiService;

  @tracked displayName = '';
  @tracked email = '';
  @tracked password = '';
  @tracked preferredLanguage = 'it';
  @tracked error: string | null = null;

  @action updateDisplayName(event: Event) {
    this.displayName = (event.target as HTMLInputElement).value;
  }

  @action updateEmail(event: Event) {
    this.email = (event.target as HTMLInputElement).value;
  }

  @action updatePassword(event: Event) {
    this.password = (event.target as HTMLInputElement).value;
  }

  @action updateLanguage(event: Event) {
    this.preferredLanguage = (event.target as HTMLSelectElement).value;
  }

  @action
  async submit() {
    this.error = null;
    try {
      await this.api.register({
        displayName: this.displayName,
        email: this.email,
        password: this.password,
        preferredLanguage: this.preferredLanguage,
      });
    } catch (error) {
      this.error = error instanceof Error ? error.message : 'Registrazione fallita';
    }
  }

  <template>
    <section class="page narrow">
      <header class="page-header">
        <h2>{{t "auth.register"}}</h2>
        <p>{{t "auth.register"}}</p>
      </header>

      <div class="card">
        <label>
          Nome
          <input type="text" value={{this.displayName}} {{on "input" this.updateDisplayName}} />
        </label>
        <label>
          {{t "auth.email"}}
          <input type="email" value={{this.email}} {{on "input" this.updateEmail}} />
        </label>
        <label>
          {{t "auth.password"}}
          <input type="password" value={{this.password}} {{on "input" this.updatePassword}} />
        </label>
        <label>
          {{t "settings.language"}}
          <select value={{this.preferredLanguage}} {{on "change" this.updateLanguage}}>
            <option value="it">Italiano</option>
            <option value="en">English</option>
          </select>
        </label>
        {{#if this.error}}
          <p class="error">{{this.error}}</p>
        {{/if}}
        <button type="button" class="primary" {{on "click" this.submit}}>{{t "auth.register"}}</button>
      </div>
    </section>
  </template>
}
