import { computed, inject, Injectable, signal } from '@angular/core';

import { CollectionsApi } from '../../core/api/api.clients';
import type { CollectionItem } from '../../core/api/models';
import type { LoadStatus } from '../discover/discovery.store';

export type CollectionKind = 'reading' | 'history';
export type CollectionSort = 'newest' | 'title';

@Injectable()
export class CollectionStore {
  private readonly api = inject(CollectionsApi);
  readonly items = signal<readonly CollectionItem[]>([]);
  readonly status = signal<LoadStatus>('loading');
  readonly query = signal('');
  readonly sort = signal<CollectionSort>('newest');
  readonly visibleItems = computed(() => {
    const needle = this.query().trim().toLocaleLowerCase();
    const filtered = this.items().filter((item) => {
      if (!needle) return true;
      const haystack = [item.title, item.authors.join(' '), item.catalogBookId].join(' ').toLocaleLowerCase();
      return haystack.includes(needle);
    });
    return [...filtered].sort((left, right) => this.sort() === 'title'
      ? this.titleOf(left).localeCompare(this.titleOf(right))
      : this.addedAt(right).localeCompare(this.addedAt(left)));
  });

  async load(kind: CollectionKind): Promise<void> {
    this.status.set('loading');
    try {
      const items = kind === 'reading' ? await this.api.readingList() : await this.api.discovered();
      this.items.set(items);
      this.status.set(items.length ? 'ready' : 'empty');
    } catch (error: unknown) {
      this.status.set(navigator.onLine ? 'error' : 'offline');
    }
  }

  async remove(catalogBookId: string): Promise<void> {
    const existing = this.items();
    this.items.set(existing.filter((item) => item.catalogBookId !== catalogBookId));
    try {
      await this.api.remove(catalogBookId);
    } catch (error: unknown) {
      this.items.set(existing);
      this.status.set(navigator.onLine ? 'error' : 'offline');
    }
  }

  async removeDiscovered(catalogBookId: string): Promise<void> {
    const existing = this.items();
    this.items.set(existing.filter((item) => item.catalogBookId !== catalogBookId));
    try {
      await this.api.removeDiscovered(catalogBookId);
    } catch (error: unknown) {
      this.items.set(existing);
      this.status.set(navigator.onLine ? 'error' : 'offline');
    }
  }

  private addedAt(item: CollectionItem): string {
    return 'addedAt' in item ? item.addedAt : item.discoveredAt;
  }

  private titleOf(item: CollectionItem): string {
    return item.title || item.catalogBookId;
  }
}
