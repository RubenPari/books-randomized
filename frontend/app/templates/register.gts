/** Registration page: collects display name, email, password, and language preference. */
import { on } from '@ember/modifier';
import t from '../helpers/t';

<template>
  <section class="page narrow">
    <header class="page-header">
      <h2>{{t "auth.register"}}</h2>
      <p>{{t "auth.register"}}</p>
    </header>

    <div class="card">
      <label>
        Nome
        <input type="text" value={{this.displayName}} {{on "input" this.updateDisplayName}} />
      </label>
      <label>
        {{t "auth.email"}}
        <input type="email" value={{this.email}} {{on "input" this.updateEmail}} />
      </label>
      <label>
        {{t "auth.password"}}
        <input type="password" value={{this.password}} {{on "input" this.updatePassword}} />
      </label>
      <label>
        {{t "settings.language"}}
        <select value={{this.preferredLanguage}} {{on "change" this.updateLanguage}}>
          <option value="it">Italiano</option>
          <option value="en">English</option>
        </select>
      </label>
      {{#if this.error}}
        <p class="error">{{this.error}}</p>
      {{/if}}
      <button type="button" class="primary" {{on "click" this.submit}}>{{t "auth.register"}}</button>
    </div>
  </section>
</template>
