import Controller from '@ember/controller';
import { action } from '@ember/object';
import { inject as service } from '@ember/service';
import { tracked } from '@glimmer/tracking';
import type ApiService from '../services/api';
import type I18nService from '../services/i18n';
import type DiscoveryHistoryService from '../services/discovery-history';
import type AuthService from '../services/auth';

interface Book {
  id: string;
  externalId: string;
  title: string;
  description?: string;
  coverUrl?: string;
  authors?: string[];
}

/**
 * Controller for the random book discovery page.
 * Manages filter state and actions for fetching a random book and saving it to the vault.
 * Passes the session-scoped exclude list to prevent duplicate suggestions.
 */
export default class RandomController extends Controller {
  @service declare api: ApiService;
  @service declare i18n: I18nService;
  @service declare discoveryHistory: DiscoveryHistoryService;
  @service declare auth: AuthService;

  @tracked category = '';
  @tracked language = 'it';
  @tracked minRating = '';
  @tracked yearFrom = '';
  @tracked yearTo = '';
  @tracked currentBook: Book | null = null;
  @tracked isSaving = false;

  @action
  async fetchRandomBook() {
    const book = (await this.api.getRandomBook({
      category: this.category || undefined,
      language: this.language || undefined,
      minRating: this.minRating || undefined,
      yearFrom: this.yearFrom || undefined,
      yearTo: this.yearTo || undefined,
      targetLanguage: this.i18n.locale,
      // Pass session history to backend to avoid duplicates (optional, backend also tracks for user)
      excludeIds: this.discoveryHistory.ids.join(','),
    })) as Book;

    this.currentBook = book;
    if (book.externalId) {
      this.discoveryHistory.add(book);
    }
  }

  @action
  async saveToVault() {
    if (!this.currentBook || !this.auth.accessToken) {
      if (!this.auth.accessToken) {
        alert(this.i18n.locale === 'it' ? 'Devi accedere per salvare nel Vault.' : 'You must login to save to Vault.');
      }
      return;
    }

    this.isSaving = true;
    try {
      await this.api.addToVault(this.currentBook.externalId);
      alert(this.i18n.locale === 'it' ? 'Aggiunto al Vault!' : 'Added to Vault!');
    } catch (e: any) {
      alert(e.message);
    } finally {
      this.isSaving = false;
    }
  }

  @action updateCategory(event: Event) {
    this.category = (event.target as HTMLInputElement).value;
  }

  @action updateLanguage(event: Event) {
    this.language = (event.target as HTMLSelectElement).value;
  }

  @action updateMinRating(event: Event) {
    this.minRating = (event.target as HTMLInputElement).value;
  }

  @action updateYearFrom(event: Event) {
    this.yearFrom = (event.target as HTMLInputElement).value;
  }

  @action updateYearTo(event: Event) {
    this.yearTo = (event.target as HTMLInputElement).value;
  }
}
