import Service from '@ember/service';
import { tracked } from '@glimmer/tracking';
import type { BookSummary } from '../../types/book';

/**
 * Session-scoped discovery history stored in sessionStorage.
 * Tracks books discovered during the current browser session to help
 * the backend avoid suggesting duplicates, even for anonymous users.
 */
export default class DiscoveryHistoryService extends Service {
  @tracked sessionHistory: BookSummary[] = [];

  constructor() {
    super();
    const stored = window.sessionStorage.getItem('discoveryHistory');
    if (stored) {
      try {
        this.sessionHistory = JSON.parse(stored) as BookSummary[];
      } catch {
        this.sessionHistory = [];
      }
    }
  }

  /** Prepends a book to the session history if not already present (deduplicated by externalId). */
  add(book: BookSummary) {
    if (!this.sessionHistory.find((b) => b.externalId === book.externalId)) {
      this.sessionHistory = [book, ...this.sessionHistory];
      window.sessionStorage.setItem(
        'discoveryHistory',
        JSON.stringify(this.sessionHistory)
      );
    }
  }

  get ids() {
    return this.sessionHistory.map((b) => b.externalId);
  }
}
