/** Random book discovery page: filter inputs, generate button, and book preview card with save-to-vault action. */
import { on } from '@ember/modifier';
import t from '../helpers/t';

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
        <div class="cover" style={{if this.currentBook.coverUrl (concat "background-image: url('" this.currentBook.coverUrl "')")}}></div>
        <div class="details">
          <h3>{{this.currentBook.title}}</h3>
          {{#if this.currentBook.authors}}
            <p class="meta">{{this.currentBook.authors}}</p>
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
