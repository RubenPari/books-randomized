import Controller from '@ember/controller';
import { action } from '@ember/object';
import { inject as service } from '@ember/service';
import { tracked } from '@glimmer/tracking';
import type ApiService from '../services/api';

export default class ResetController extends Controller {
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
}
