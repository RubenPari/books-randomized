import Component from '@glimmer/component';
import { tracked } from '@glimmer/tracking';
import { action } from '@ember/object';
import { service } from '@ember/service';
import { on } from '@ember/modifier';
import type ApiService from '../services/api';

export default class ResetPage extends Component {
  @service declare api: ApiService;

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
      await this.api.request('/auth/reset/request', {
        method: 'POST',
        body: JSON.stringify({ email: this.email }),
      });
      this.message = 'Token inviato alla tua email.';
    } catch (error) {
      this.error = error instanceof Error ? error.message : 'Invio fallito';
    }
  }

  <template>
    <section class="page narrow">
      <header class="page-header">
        <h2>Reset password</h2>
        <p>Ricevi un token per impostare una nuova password.</p>
      </header>

      <div class="card">
        <label>
          Email
          <input type="email" value={{this.email}} {{on "input" this.updateEmail}} />
        </label>
        {{#if this.message}}
          <p class="success">{{this.message}}</p>
        {{/if}}
        {{#if this.error}}
          <p class="error">{{this.error}}</p>
        {{/if}}
        <button type="button" class="primary" {{on "click" this.submit}}>Invia token</button>
      </div>
    </section>
  </template>
}
