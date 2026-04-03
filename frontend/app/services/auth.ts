import Service from '@ember/service';
import { tracked } from '@glimmer/tracking';

export default class AuthService extends Service {
  @tracked accessToken: string | null = null;
  @tracked refreshToken: string | null = null;

  constructor() {
    super();
    this.accessToken = window.localStorage.getItem('accessToken');
    this.refreshToken = window.localStorage.getItem('refreshToken');
  }

  setTokens(accessToken: string, refreshToken: string) {
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    window.localStorage.setItem('accessToken', accessToken);
    window.localStorage.setItem('refreshToken', refreshToken);
  }

  clear() {
    this.accessToken = null;
    this.refreshToken = null;
    window.localStorage.removeItem('accessToken');
    window.localStorage.removeItem('refreshToken');
  }
}
