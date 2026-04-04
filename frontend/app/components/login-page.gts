import Component from '@glimmer/component';
import { tracked } from '@glimmer/tracking';
import { action } from '@ember/object';
import { service } from '@ember/service';
import { on } from '@ember/modifier';
import t from '../helpers/t';
import type ApiService from '../services/api';

export default class LoginPage extends Component {
  @service declare api: ApiService;

  @tracked email = '';
  @tracked password = '';
  @tracked error: string | null = null;

  @action
  updateEmail(event: Event) {
    this.email = (event.target as HTMLInputElement).value;
  }

  @action
  updatePassword(event: Event) {
    this.password = (event.target as HTMLInputElement).value;
  }

  @action
  async submit() {
    this.error = null;
    try {
      await this.api.login(this.email, this.password);
    } catch (error) {
      this.error = error instanceof Error ? error.message : 'Login fallito';
    }
  }

  <template>
    <section class="page narrow">
      <header class="page-header">
        <h2>{{t "auth.login"}}</h2>
        <p>{{t "auth.login"}}</p>
      </header>

      <div class="card">
        <label>
          {{t "auth.email"}}
          <input type="email" placeholder="you@email.com" value={{this.email}} {{on "input" this.updateEmail}} />
        </label>
        <label>
          {{t "auth.password"}}
          <input type="password" value={{this.password}} {{on "input" this.updatePassword}} />
        </label>
        {{#if this.error}}
          <p class="error">{{this.error}}</p>
        {{/if}}
        <button type="button" class="primary" {{on "click" this.submit}}>{{t "auth.login"}}</button>
      </div>
    </section>
  </template>
}
