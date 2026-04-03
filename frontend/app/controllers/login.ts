import Controller from '@ember/controller';
import { action } from '@ember/object';
import { inject as service } from '@ember/service';
import { tracked } from '@glimmer/tracking';
import type ApiService from '../services/api';

export default class LoginController extends Controller {
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
}
