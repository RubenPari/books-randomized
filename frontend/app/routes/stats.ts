import Route from '@ember/routing/route';
import { inject as service } from '@ember/service';
import type ApiService from '../services/api';

export default class StatsRoute extends Route {
  @service declare api: ApiService;

  async model() {
    return this.api.stats();
  }
}
