/** Settings page: language selector and preferred category input. */
import { on } from '@ember/modifier';
import t from '../helpers/t';

<template>
  <section class="page">
    <header class="page-header">
      <h2>{{t "settings.title"}}</h2>
      <p>{{t "settings.description"}}</p>
    </header>

    <div class="card">
      <label>
        {{t "settings.language"}}
        <select value={{this.locale}} {{on "change" this.updateLocale}}>
          <option value="it">Italiano</option>
          <option value="en">English</option>
        </select>
      </label>
      <label>
        {{t "settings.preferredCategory"}}
        <input type="text" placeholder="Es. Sci-Fi" />
      </label>
    </div>
  </section>
</template>
