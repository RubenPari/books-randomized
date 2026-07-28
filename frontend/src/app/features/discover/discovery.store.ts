import { computed, inject, Injectable, signal } from '@angular/core';

import { CollectionsApi, DiscoveryApi } from '../../core/api/api.clients';
import type { Book, BookFilters, FeedbackValue } from '../../core/api/models';

export type LoadStatus = 'idle' | 'loading' | 'ready' | 'empty' | 'error' | 'offline';

@Injectable()
export class DiscoveryStore {
  private readonly api = inject(DiscoveryApi);
  private readonly collections = inject(CollectionsApi);
  private requestSequence = 0;
  readonly book = signal<Book | null>(null);
  readonly filters = signal<BookFilters>({});
  readonly status = signal<LoadStatus>('idle');
  readonly saved = signal(false);
  readonly feedback = signal<FeedbackValue | null>(null);
  readonly actionPending = signal(false);
  readonly hasResult = computed(() => this.book() !== null);

  updateFilters(filters: BookFilters): void {
    this.filters.set(filters);
  }

  async discover(): Promise<void> {
    const request = ++this.requestSequence;
    this.status.set('loading');
    this.saved.set(false);
    this.feedback.set(null);
    try {
      const book = await this.api.random(this.filters());
      if (request !== this.requestSequence) return;
      this.book.set(book);
      this.status.set('ready');
    } catch (error: unknown) {
      if (request !== this.requestSequence) return;
      this.status.set(navigator.onLine ? 'error' : 'offline');
    }
  }

  async save(): Promise<void> {
    const book = this.book();
    if (!book || this.actionPending() || this.saved()) return;
    this.actionPending.set(true);
    try {
      await this.collections.save(book.workId, book.title, book.authors);
      this.saved.set(true);
    } finally {
      this.actionPending.set(false);
    }
  }

  async sendFeedback(value: FeedbackValue): Promise<void> {
    const book = this.book();
    if (!book || this.actionPending() || this.feedback() === value) return;
    this.actionPending.set(true);
    try {
      await this.collections.feedback(book.workId, value);
      this.feedback.set(value);
    } finally {
      this.actionPending.set(false);
    }
  }
}
