import Service from '@ember/service';
import { service } from '@ember/service';
import config from 'frontend/config/environment';
import type {
  AuthResponse,
  BookResponse,
  DiscoveryResponse,
  StatsResponse,
  VaultEntryResponse,
  VaultImportEntryPayload,
} from '../../types/api';
import type AuthService from './auth';

/**
 * Central HTTP service wrapping all backend API calls.
 * Automatically attaches the JWT Bearer token from the auth service
 * and provides typed methods for every backend endpoint.
 */
export default class ApiService extends Service {
  @service declare auth: AuthService;

  private get baseUrl(): string {
    return config.APP.apiBaseUrl as string;
  }

  async getRandomBook(
    filters: Record<string, string | number | undefined> = {}
  ): Promise<BookResponse> {
    const params = new URLSearchParams();
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== undefined && value !== '') {
        params.append(key, String(value));
      }
    });
    const query = params.toString();
    return this.request(`/books/random${query ? `?${query}` : ''}`);
  }

  async login(email: string, password: string): Promise<AuthResponse> {
    const response = await this.request<AuthResponse>('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    });
    this.auth.setTokens(response.accessToken, response.refreshToken);
    return response;
  }

  async register(payload: Record<string, unknown>): Promise<AuthResponse> {
    const response = await this.request<AuthResponse>('/auth/register', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
    this.auth.setTokens(response.accessToken, response.refreshToken);
    return response;
  }

  async listHistory(): Promise<DiscoveryResponse[]> {
    return this.request('/history');
  }

  async listVault(): Promise<VaultEntryResponse[]> {
    return this.request('/vault');
  }

  async stats(): Promise<StatsResponse> {
    return this.request('/stats');
  }

  async addToVault(
    externalBookId: string,
    note?: string,
    personalRating?: number
  ): Promise<VaultEntryResponse> {
    return this.request('/vault', {
      method: 'POST',
      body: JSON.stringify({ externalBookId, note, personalRating }),
    });
  }

  async removeFromVault(entryId: string): Promise<void> {
    await this.request(`/vault/${entryId}`, {
      method: 'DELETE',
    });
  }

  async importVault(entries: VaultImportEntryPayload[]): Promise<void> {
    await this.request('/vault/import', {
      method: 'POST',
      body: JSON.stringify({ entries }),
    });
  }

  async requestPasswordReset(email: string): Promise<void> {
    await this.request('/auth/reset/request', {
      method: 'POST',
      body: JSON.stringify({ email }),
    });
  }

  /**
   * Low-level fetch wrapper: injects auth header, optional refresh on 401,
   * handles errors and 204 responses.
   */
  async request<T = unknown>(
    path: string,
    options: RequestInit & { __retryAfterRefresh?: boolean } = {}
  ): Promise<T> {
    const { __retryAfterRefresh, ...fetchOptions } = options;
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      ...(fetchOptions.headers as Record<string, string> | undefined),
    };

    if (this.auth.accessToken) {
      headers.Authorization = `Bearer ${this.auth.accessToken}`;
    }

    const response = await fetch(`${this.baseUrl}${path}`, {
      ...fetchOptions,
      headers,
    });

    if (
      response.status === 401 &&
      !__retryAfterRefresh &&
      this.shouldTryRefresh(path)
    ) {
      try {
        await this.refreshAccessToken();
        return this.request<T>(path, { ...options, __retryAfterRefresh: true });
      } catch {
        this.auth.clear();
        throw new Error('Session expired');
      }
    }

    if (!response.ok) {
      const errorBody = (await response.json().catch(() => ({}))) as {
        error?: string;
      };
      throw new Error(errorBody.error || 'Request failed');
    }

    if (response.status === 204) {
      return {} as T;
    }

    return response.json() as Promise<T>;
  }

  private shouldTryRefresh(path: string): boolean {
    if (!this.auth.refreshToken) {
      return false;
    }
    if (path.startsWith('/auth/')) {
      return false;
    }
    return true;
  }

  private async refreshAccessToken(): Promise<void> {
    const refreshToken = this.auth.refreshToken;
    if (!refreshToken) {
      throw new Error('No refresh token');
    }
    const response = await fetch(`${this.baseUrl}/auth/refresh`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken }),
    });
    if (!response.ok) {
      const errorBody = (await response.json().catch(() => ({}))) as {
        error?: string;
      };
      throw new Error(errorBody.error || 'Refresh failed');
    }
    const data = (await response.json()) as AuthResponse;
    this.auth.setTokens(data.accessToken, data.refreshToken);
  }
}
