/**
 * QUnit test bootstrap module.
 * Creates the Ember application instance, registers qunit-dom assertions,
 * configures Ember.onerror validation, and starts the QUnit test runner.
 */
import '@warp-drive/ember/install';
import Application from 'frontend/app';
import config from 'frontend/config/environment';
import * as QUnit from 'qunit';
import { setApplication } from '@ember/test-helpers';
import { setup } from 'qunit-dom';
import { start as qunitStart, setupEmberOnerrorValidation } from 'ember-qunit';

export function start() {
  setApplication(Application.create(config.APP));

  setup(QUnit.assert);
  setupEmberOnerrorValidation();

  qunitStart();
}
