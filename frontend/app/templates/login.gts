import { on } from '@ember/modifier';
import t from '../helpers/t';

<template>
  <section class="page narrow">
    <header class="page-header">
      <h2>{{t "auth.login"}}</h2>
      <p>{{t "auth.login"}}</p>
    </header>

    <div class="card">
      <label>
        {{t "auth.email"}}
        <input type="email" placeholder="you@email.com" value={{this.email}} {{on "input" this.updateEmail}} />
      </label>
      <label>
        {{t "auth.password"}}
        <input type="password" value={{this.password}} {{on "input" this.updatePassword}} />
      </label>
      {{#if this.error}}
        <p class="error">{{this.error}}</p>
      {{/if}}
      <button type="button" class="primary" {{on "click" this.submit}}>{{t "auth.login"}}</button>
    </div>
  </section>
</template>
