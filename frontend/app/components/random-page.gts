import Component from '@glimmer/component';
import { helper } from '@ember/component/helper';
import { tracked } from '@glimmer/tracking';
import { action } from '@ember/object';
import { service } from '@ember/service';
import { htmlSafe, type SafeString } from '@ember/template';
import { on } from '@ember/modifier';
import DOMPurify from 'dompurify';
import t from '../helpers/t';
import type ApiService from '../services/api';
import type I18nService from '../services/i18n';
import type DiscoveryHistoryService from '../services/discovery-history';
import type AuthService from '../services/auth';
import type TranslationsService from '../services/translations';
import { BOOK_CATEGORY_OPTIONS } from '../lib/book-category-options';
import type { BookResponse } from '../../types/api';
import type { BookSummary } from '../../types/book';

/** Template helper: strict-mode templates cannot call component methods with args (loses `this`). */
const categoryChecked = helper(function (positional: unknown[]) {
  const selected = positional[0] as string[] | undefined;
  const value = positional[1] as string | undefined;
  if (!value || !selected?.length) {
    return false;
  }
  return selected.includes(value);
});

export default class RandomPage extends Component {
  @service declare api: ApiService;
  @service declare i18n: I18nService;
  @service declare discoveryHistory: DiscoveryHistoryService;
  @service declare auth: AuthService;
  @service declare translations: TranslationsService;

  readonly categoryOptions = BOOK_CATEGORY_OPTIONS;

  @tracked selectedCategories: string[] = [];
  @tracked minRating = '';
  @tracked yearFrom = '';
  @tracked yearTo = '';
  @tracked currentBook: BookResponse | null = null;
  @tracked isSaving = false;
  @tracked coverLoadFailed = false;
  @tracked saveFeedback: string | null = null;
  @tracked saveError: string | null = null;

  get showCoverImage(): boolean {
    return Boolean(this.currentBook?.coverUrl && !this.coverLoadFailed);
  }

  get authorsLine(): string {
    const a = this.currentBook?.authors;
    if (!a?.length) {
      return '';
    }
    return Array.isArray(a) ? a.join(', ') : String(a);
  }

  get categoriesLine(): string {
    const c = this.currentBook?.categories;
    if (!c?.length) {
      return '';
    }
    return Array.isArray(c) ? c.join(' · ') : String(c);
  }

  /** ISBNdb / API descriptions are often HTML; mark safe so tags render (not escaped). */
  get descriptionHtml(): SafeString {
    const raw = this.currentBook?.description?.trim();
    if (!raw) {
      return htmlSafe('');
    }
    const clean = DOMPurify.sanitize(raw, { USE_PROFILES: { html: true } });
    return htmlSafe(clean);
  }

  @action
  onCoverError() {
    this.coverLoadFailed = true;
  }

  @action
  async fetchRandomBook() {
    this.coverLoadFailed = false;
    this.saveFeedback = null;
    this.saveError = null;
    const book = await this.api.getRandomBook({
      category:
        this.selectedCategories.length > 0
          ? this.selectedCategories.join(',')
          : undefined,
      minRating: this.minRating || undefined,
      yearFrom: this.yearFrom || undefined,
      yearTo: this.yearTo || undefined,
      targetLanguage: this.i18n.locale,
      excludeIds: this.discoveryHistory.ids.join(','),
    });

    this.currentBook = book;
    if (book.externalId) {
      const summary: BookSummary = {
        id: book.id,
        externalId: book.externalId,
        title: book.title,
        description: book.description ?? undefined,
        coverUrl: book.coverUrl ?? undefined,
        authors: book.authors,
      };
      this.discoveryHistory.add(summary);
    }
  }

  @action
  async saveToVault() {
    this.saveFeedback = null;
    this.saveError = null;
    if (!this.currentBook) {
      return;
    }
    if (!this.auth.accessToken) {
      this.saveError = this.translations.t('random.saveRequiresLogin');
      return;
    }

    this.isSaving = true;
    try {
      await this.api.addToVault(this.currentBook.externalId);
      this.saveFeedback = this.translations.t('random.saveSuccess');
    } catch (e: unknown) {
      this.saveError =
        e instanceof Error
          ? e.message
          : this.translations.t('random.saveFailed');
    } finally {
      this.isSaving = false;
    }
  }

  @action
  onCategoryCheckboxChange(event: Event) {
    const input = event.target as HTMLInputElement;
    const value = input.value;
    const checked = input.checked;
    if (checked) {
      if (!this.selectedCategories.includes(value)) {
        this.selectedCategories = [...this.selectedCategories, value];
      }
    } else {
      this.selectedCategories = this.selectedCategories.filter(
        (v) => v !== value
      );
    }
  }

  @action
  clearCategories() {
    this.selectedCategories = [];
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
          <fieldset class="category-multiselect">
            <legend>{{t "random.category"}}</legend>
            <p class="category-hint">{{t "random.categoryHint"}}</p>
            <div class="category-checkboxes">
              {{#each this.categoryOptions as |opt|}}
                <label class="category-option">
                  <input
                    type="checkbox"
                    value={{opt.value}}
                    checked={{categoryChecked
                      this.selectedCategories
                      opt.value
                    }}
                    {{on "change" this.onCategoryCheckboxChange}}
                  />
                  <span>{{t opt.labelKey}}</span>
                </label>
              {{/each}}
            </div>
            {{#if this.selectedCategories.length}}
              <button
                type="button"
                class="category-clear"
                {{on "click" this.clearCategories}}
              >
                {{t "random.categoryClear"}}
              </button>
            {{/if}}
          </fieldset>
          <label>
            {{t "random.minRating"}}
            <input
              type="number"
              min="0"
              max="5"
              step="0.1"
              value={{this.minRating}}
              {{on "input" this.updateMinRating}}
            />
          </label>
          <label>
            {{t "random.yearFrom"}}
            <input
              type="number"
              value={{this.yearFrom}}
              {{on "input" this.updateYearFrom}}
            />
          </label>
          <label>
            {{t "random.yearTo"}}
            <input
              type="number"
              value={{this.yearTo}}
              {{on "input" this.updateYearTo}}
            />
          </label>
        </div>

        <button
          type="button"
          class="primary"
          {{on "click" this.fetchRandomBook}}
        >
          {{t "random.generate"}}
        </button>
      </div>

      <div class="card book-preview">
        {{#if this.currentBook}}
          <div class="cover {{unless this.showCoverImage 'cover-placeholder'}}">
            {{#if this.showCoverImage}}
              <img
                src={{this.currentBook.coverUrl}}
                alt={{this.currentBook.title}}
                loading="lazy"
                decoding="async"
                {{on "error" this.onCoverError}}
              />
            {{/if}}
          </div>
          <div class="details">
            <div class="details-head">
              <h3>{{this.currentBook.title}}</h3>
              {{#if this.authorsLine}}
                <p class="meta authors">{{this.authorsLine}}</p>
              {{/if}}
              <div class="meta-row">
                {{#if this.currentBook.publicationYear}}
                  <span class="chip">{{this.currentBook.publicationYear}}</span>
                {{/if}}
                {{#if this.currentBook.language}}
                  <span class="chip">{{this.currentBook.language}}</span>
                {{/if}}
                {{#if this.currentBook.rating}}
                  <span class="chip">★ {{this.currentBook.rating}}</span>
                {{/if}}
              </div>
              {{#if this.categoriesLine}}
                <p class="meta categories">{{this.categoriesLine}}</p>
              {{/if}}
            </div>
            {{#if this.currentBook.description}}
              <div class="description">{{this.descriptionHtml}}</div>
            {{/if}}
            <button
              type="button"
              class="secondary"
              disabled={{this.isSaving}}
              {{on "click" this.saveToVault}}
            >
              {{t "random.saveToVault"}}
            </button>
            {{#if this.saveFeedback}}
              <p class="success">{{this.saveFeedback}}</p>
            {{/if}}
            {{#if this.saveError}}
              <p class="error">{{this.saveError}}</p>
            {{/if}}
          </div>
        {{else}}
          <div class="cover cover-placeholder cover-empty-state"></div>
          <div class="details">
            <h3>{{t "random.noBook"}}</h3>
            <p>{{t "random.noBookDesc"}}</p>
          </div>
        {{/if}}
      </div>
    </section>
  </template>
}
