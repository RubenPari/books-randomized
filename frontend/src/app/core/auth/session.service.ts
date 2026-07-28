import { inject, Injectable, signal } from '@angular/core';

import { AuthApi } from '../api/api.clients';
import type { Credentials, User } from '../api/models';

@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly api = inject(AuthApi);
  private refreshPromise: Promise<string | null> | null = null;
  private csrfReady = false;
  readonly accessToken = signal<string | null>(null);
  readonly user = signal<User | null>(null);

  async login(credentials: Credentials): Promise<void> {
    const response = await this.api.login(credentials);
    this.accessToken.set(response.accessToken);
    this.user.set(response.user);
  }

  async register(credentials: Credentials): Promise<void> {
    const response = await this.api.register(credentials);
    this.accessToken.set(response.accessToken);
    this.user.set(response.user);
  }

  async ensureCsrf(): Promise<void> {
    if (this.csrfReady) return;
    await this.api.csrf();
    this.csrfReady = true;
  }

  async refresh(): Promise<string | null> {
    await this.ensureCsrf();
    if (this.refreshPromise) return this.refreshPromise;
    this.refreshPromise = this.performRefresh();
    const token = await this.refreshPromise;
    this.refreshPromise = null;
    return token;
  }

  async ensureSession(): Promise<boolean> {
    await this.ensureCsrf();
    return this.accessToken() !== null || (await this.refresh()) !== null;
  }

  async logout(): Promise<void> {
    try {
      await this.ensureCsrf();
      await this.api.logout();
    } finally {
      this.clear();
    }
  }

  clear(): void {
    this.accessToken.set(null);
    this.user.set(null);
  }

  private async performRefresh(): Promise<string | null> {
    try {
      const response = await this.api.refresh();
      this.accessToken.set(response.accessToken);
      this.user.set(response.user);
      return response.accessToken;
    } catch (error: unknown) {
      this.clear();
      return null;
    }
  }
}
