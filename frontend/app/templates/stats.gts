import t from '../helpers/t';

<template>
  <section class="page">
    <header class="page-header">
      <h2>{{t "stats.title"}}</h2>
      <p>{{t "stats.description"}}</p>
    </header>

    <div class="stats-grid">
      <div class="card stat">
        <h3>{{t "stats.totalDiscovered"}}</h3>
        <p>{{@model.totalDiscovered}}</p>
      </div>
      <div class="card stat">
        <h3>{{t "stats.averageRating"}}</h3>
        <p>{{@model.averageRating}}</p>
      </div>
      <div class="card stat">
        <h3>{{t "stats.totalVaulted"}}</h3>
        <p>{{@model.totalVaulted}}</p>
      </div>
    </div>

    <div class="stats-distributions">
      <div class="card">
        <h3>Categorie</h3>
        <ul>
          {{#each-in @model.byCategory as |category count|}}
            <li>{{category}}: {{count}}</li>
          {{/each-in}}
        </ul>
      </div>
      <div class="card">
        <h3>Autori</h3>
        <ul>
          {{#each-in @model.byAuthor as |author count|}}
            <li>{{author}}: {{count}}</li>
          {{/each-in}}
        </ul>
      </div>
      <div class="card">
        <h3>Lingue</h3>
        <ul>
          {{#each-in @model.byLanguage as |lang count|}}
            <li>{{lang}}: {{count}}</li>
          {{/each-in}}
        </ul>
      </div>
    </div>
  </section>
</template>
