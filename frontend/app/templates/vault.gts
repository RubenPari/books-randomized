import { on } from '@ember/modifier';
import t from '../helpers/t';

<template>
  <section class="page">
    <header class="page-header">
      <h2>{{t "vault.title"}}</h2>
      <p>{{t "vault.description"}}</p>
      <div class="actions">
        <button type="button" class="secondary" {{on "click" this.exportVault}}>
          {{t "vault.export"}}
        </button>
        <label class="button secondary">
          {{t "vault.import"}}
          <input type="file" accept=".json" hidden {{on "change" this.importVault}} />
        </label>
      </div>
    </header>

    {{#if @model.length}}
      <div class="list">
        {{#each @model as |entry|}}
          <div class="card list-item">
            <h3>{{entry.book.title}}</h3>
            <p class="meta">{{entry.book.authors}}</p>
            {{#if entry.note}}
              <p>{{entry.note}}</p>
            {{/if}}
            {{#if entry.personalRating}}
              <p>Rating: {{entry.personalRating}}</p>
            {{/if}}
          </div>
        {{/each}}
      </div>
    {{else}}
      <div class="card empty">
        <p>{{t "vault.empty"}}</p>
      </div>
    {{/if}}
  </section>
</template>
