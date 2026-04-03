import Controller from '@ember/controller';
import { action } from '@ember/object';
import { inject as service } from '@ember/service';
import { tracked } from '@glimmer/tracking';
import type ApiService from '../services/api';

export default class RegisterController extends Controller {
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
}
