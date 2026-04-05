import Component from '@glimmer/component';
import { tracked } from '@glimmer/tracking';
import { action } from '@ember/object';
import { service } from '@ember/service';
import { on } from '@ember/modifier';
import t from '../helpers/t';
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
  categories?: string[];
  language?: string;
  rating?: number;
  publicationYear?: number;
}

export default class RandomPage extends Component {
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

  <template>
    <section class="page">
      <header class="page-header">
        <h2>{{t "random.title"}}</h2>
        <p>{{t "random.description"}}</p>
      </header>

      <div class="card">
        <div class="filters">
          <label>
            {{t "random.category"}}
            <input type="text" placeholder="Es. Fantasy" value={{this.category}} {{on "input" this.updateCategory}} />
          </label>
          <label>
            {{t "random.language"}}
            <select value={{this.language}} {{on "change" this.updateLanguage}}>
              <option value="it">Italiano</option>
              <option value="en">English</option>
            </select>
          </label>
          <label>
            {{t "random.minRating"}}
            <input type="number" min="0" max="5" step="0.1" value={{this.minRating}} {{on "input" this.updateMinRating}} />
          </label>
          <label>
            {{t "random.yearFrom"}}
            <input type="number" value={{this.yearFrom}} {{on "input" this.updateYearFrom}} />
          </label>
          <label>
            {{t "random.yearTo"}}
            <input type="number" value={{this.yearTo}} {{on "input" this.updateYearTo}} />
          </label>
        </div>

        <button type="button" class="primary" {{on "click" this.fetchRandomBook}}>
          {{t "random.generate"}}
        </button>
      </div>

      <div class="card book-preview">
        {{#if this.currentBook}}
          <div class="cover">
            {{#if this.currentBook.coverUrl}}
              <img src={{this.currentBook.coverUrl}} alt={{this.currentBook.title}} />
            {{/if}}
          </div>
          <div class="details">
            <h3>{{this.currentBook.title}}</h3>
            {{#if this.currentBook.authors}}
              <p class="meta">{{this.currentBook.authors}}</p>
            {{/if}}
            {{#if this.currentBook.categories}}
              <p class="meta">{{this.currentBook.categories}}</p>
            {{/if}}
            {{#if this.currentBook.language}}
              <p class="meta">{{this.currentBook.language}}</p>
            {{/if}}
            {{#if this.currentBook.publicationYear}}
              <p class="meta">{{this.currentBook.publicationYear}}</p>
            {{/if}}
            <p>{{this.currentBook.description}}</p>
            <button type="button" class="secondary" {{on "click" this.saveToVault}}>
              {{t "random.saveToVault"}}
            </button>
          </div>
        {{else}}
          <div class="cover"></div>
          <div class="details">
            <h3>{{t "random.noBook"}}</h3>
            <p>{{t "random.noBookDesc"}}</p>
          </div>
        {{/if}}
      </div>
    </section>
  </template>
}
