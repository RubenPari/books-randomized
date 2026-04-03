import Route from '@ember/routing/route';
import { inject as service } from '@ember/service';
import type ApiService from '../services/api';
import type DiscoveryHistoryService from '../services/discovery-history';
import type AuthService from '../services/auth';

export default class HistoryRoute extends Route {
  @service declare api: ApiService;
  @service declare discoveryHistory: DiscoveryHistoryService;
  @service declare auth: AuthService;

  async model() {
    let remoteHistory: any[] = [];
    if (this.auth.accessToken) {
      try {
        remoteHistory = await this.api.listHistory();
      } catch (e) {
        console.error('Failed to fetch remote history', e);
      }
    }

    const sessionHistory = this.discoveryHistory.sessionHistory.map((book) => ({
      book,
      discoveredAt: new Date().toISOString(), // approximation for session books
    }));

    // Merge and deduplicate by externalId
    const seen = new Set<string>();
    const combined = [...remoteHistory, ...sessionHistory].filter((item) => {
      const extId = item.book.externalId;
      if (seen.has(extId)) return false;
      seen.add(extId);
      return true;
    });

    return combined.sort(
      (a, b) => new Date(b.discoveredAt).getTime() - new Date(a.discoveredAt).getTime()
    );
  }
}
