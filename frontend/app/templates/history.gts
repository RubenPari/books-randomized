import t from '../helpers/t';

<template>
  <section class="page">
    <header class="page-header">
      <h2>{{t "history.title"}}</h2>
      <p>{{t "history.description"}}</p>
    </header>

    {{#if @model.length}}
      <div class="list">
        {{#each @model as |item|}}
          <div class="card list-item">
            <h3>{{item.book.title}}</h3>
            {{#if item.book.authors}}
              <p class="meta">{{item.book.authors}}</p>
            {{/if}}
            <p class="date">{{item.discoveredAt}}</p>
          </div>
        {{/each}}
      </div>
    {{else}}
      <div class="card empty">
        <p>{{t "history.empty"}}</p>
      </div>
    {{/if}}
  </section>
</template>
