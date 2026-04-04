import Route from '@ember/routing/route';
import { inject as service } from '@ember/service';
import type ApiService from '../services/api';

/** Route for the statistics page. Fetches aggregated user stats from the backend as the model. */
export default class StatsRoute extends Route {
  @service declare api: ApiService;

  async model() {
    return this.api.stats();
  }
}
