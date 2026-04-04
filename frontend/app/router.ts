/**
 * Application router definition.
 * Maps URL paths to Ember routes. The "random" route is mounted at the root path ("/").
 */
import EmberRouter from '@embroider/router';
import config from 'frontend/config/environment';

export default class Router extends EmberRouter {
  location = config.locationType;
  rootURL = config.rootURL;
}

Router.map(function () {
  this.route('random', { path: '/' });
  this.route('history');
  this.route('vault');
  this.route('stats');
  this.route('login');
  this.route('register');
  this.route('reset');
  this.route('settings');
});
