import Service from '@ember/service';
import { inject as service } from '@ember/service';
import config from 'frontend/config/environment';
import type AuthService from './auth';

export default class ApiService extends Service {
  @service declare auth: AuthService;

  private get baseUrl(): string {
    return config.APP.apiBaseUrl as string;
  }

  async getRandomBook(filters: Record<string, string | number | undefined> = {}) {
    const params = new URLSearchParams();
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== undefined && value !== '') {
        params.append(key, String(value));
      }
    });
    const query = params.toString();
    return this.request(`/books/random${query ? `?${query}` : ''}`);
  }

  async login(email: string, password: string) {
    const response = await this.request('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    });
    this.auth.setTokens(response.accessToken, response.refreshToken);
    return response;
  }

  async register(payload: Record<string, unknown>) {
    const response = await this.request('/auth/register', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
    this.auth.setTokens(response.accessToken, response.refreshToken);
    return response;
  }

  async listHistory() {
    return this.request('/history');
  }

  async listVault() {
    return this.request('/vault');
  }

  async stats() {
    return this.request('/stats');
  }

  async addToVault(externalBookId: string, note?: string, personalRating?: number) {
    return this.request('/vault', {
      method: 'POST',
      body: JSON.stringify({ externalBookId, note, personalRating }),
    });
  }

  async removeFromVault(entryId: string) {
    return this.request(`/vault/${entryId}`, {
      method: 'DELETE',
    });
  }

  async importVault(entries: Array<{ externalBookId: string; note?: string; personalRating?: number }>) {
    return this.request('/vault/import', {
      method: 'POST',
      body: JSON.stringify({ entries }),
    });
  }

  async request(path: string, options: RequestInit = {}) {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      ...(options.headers as Record<string, string> | undefined),
    };

    if (this.auth.accessToken) {
      headers.Authorization = `Bearer ${this.auth.accessToken}`;
    }

    const response = await fetch(`${this.baseUrl}${path}`, {
      ...options,
      headers,
    });

    if (!response.ok) {
      const errorBody = await response.json().catch(() => ({}));
      throw new Error(errorBody.error || 'Request failed');
    }

    if (response.status === 204) {
      return {};
    }

    return response.json();
  }
}
