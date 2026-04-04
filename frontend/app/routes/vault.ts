import Route from '@ember/routing/route';
import { service } from '@ember/service';
import type ApiService from '../services/api';

/** Route for the vault page. Loads the user's saved vault entries from the backend as the model. */
export default class VaultRoute extends Route {
  @service declare api: ApiService;

  async model() {
    return this.api.listVault();
  }
}
