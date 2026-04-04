/** Password reset page: email input to request a reset token, with success/error feedback. */
import { on } from '@ember/modifier';

<template>
  <section class="page narrow">
    <header class="page-header">
      <h2>Reset password</h2>
      <p>Ricevi un token per impostare una nuova password.</p>
    </header>

    <div class="card">
      <label>
        Email
        <input type="email" value={{this.email}} {{on "input" this.updateEmail}} />
      </label>
      {{#if this.message}}
        <p class="success">{{this.message}}</p>
      {{/if}}
      {{#if this.error}}
        <p class="error">{{this.error}}</p>
      {{/if}}
      <button type="button" class="primary" {{on "click" this.submit}}>Invia token</button>
    </div>
  </section>
</template>
