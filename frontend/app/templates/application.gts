import { pageTitle } from 'ember-page-title';
import { LinkTo } from '@ember/routing';
import t from '../helpers/t';

<template>
  {{pageTitle (t "app.title")}}

  <div class="app-shell">
    <header class="app-header">
      <div class="brand">
        <span class="brand-mark">BR</span>
        <div class="brand-text">
          <h1>{{t "app.title"}}</h1>
          <p>{{t "app.tagline"}}</p>
        </div>
      </div>
      <nav class="nav">
        <LinkTo @route="random">{{t "nav.random"}}</LinkTo>
        <LinkTo @route="history">{{t "nav.history"}}</LinkTo>
        <LinkTo @route="vault">{{t "nav.vault"}}</LinkTo>
        <LinkTo @route="stats">{{t "nav.stats"}}</LinkTo>
        <LinkTo @route="settings">{{t "nav.settings"}}</LinkTo>
      </nav>
      <div class="auth-links">
        <LinkTo @route="login">{{t "nav.login"}}</LinkTo>
        <LinkTo @route="register">{{t "nav.register"}}</LinkTo>
      </div>
    </header>

    <main class="app-main">
      {{outlet}}
    </main>
  </div>
</template>
