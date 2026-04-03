import Service from '@ember/service';
import { tracked } from '@glimmer/tracking';

interface Book {
  id: string;
  externalId: string;
  title: string;
  description?: string;
  coverUrl?: string;
  authors?: string[];
}

export default class DiscoveryHistoryService extends Service {
  @tracked sessionHistory: Book[] = [];

  constructor() {
    super();
    const stored = window.sessionStorage.getItem('discoveryHistory');
    if (stored) {
      try {
        this.sessionHistory = JSON.parse(stored);
      } catch {
        this.sessionHistory = [];
      }
    }
  }

  add(book: Book) {
    if (!this.sessionHistory.find((b) => b.externalId === book.externalId)) {
      this.sessionHistory = [book, ...this.sessionHistory];
      window.sessionStorage.setItem('discoveryHistory', JSON.stringify(this.sessionHistory));
    }
  }

  get ids() {
    return this.sessionHistory.map((b) => b.externalId);
  }
}
