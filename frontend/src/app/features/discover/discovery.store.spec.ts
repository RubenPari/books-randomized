import { TestBed } from '@angular/core/testing';
import { describe, expect, it } from 'vitest';

import { CollectionsApi, DiscoveryApi } from '../../core/api/api.clients';
import { DiscoveryStore } from './discovery.store';

describe('DiscoveryStore', () => {
  it('ignores a stale discovery response after a newer request wins', async () => {
    let resolveFirst: ((value: never) => void) | undefined;
    const first = new Promise<never>((resolve) => { resolveFirst = resolve; });
    const secondBook = {
      workId: 'OL2W', title: 'Second', authors: ['Author'], subjects: ['Fiction'],
      coverUrl: null, publicationYear: 2020, rating: 4.2, ratingsCount: 10, pageCount: 220,
      languages: ['eng'], explanationKeys: ['discovery.reason.subject'],
    } as const;
    let call = 0;
    TestBed.configureTestingModule({
      providers: [
        DiscoveryStore,
        { provide: DiscoveryApi, useValue: { random: () => ++call === 1 ? first : Promise.resolve(secondBook) } },
      ],
    });
    const store = TestBed.inject(DiscoveryStore);

    const stale = store.discover();
    await store.discover();
    resolveFirst?.(secondBook as never);
    await stale;
    expect(store.book()?.workId).toBe('OL2W');
  });

  it('coalesces duplicate save clicks while the first request is pending', async () => {
    let saveCalls = 0;
    let finishSave: (() => void) | undefined;
    const pendingSave = new Promise<void>((resolve) => { finishSave = resolve; });
    const savedBook = {
      workId: 'OL3W', title: 'Saved', authors: ['Author'], subjects: [],
      coverUrl: null, publicationYear: null, rating: null, ratingsCount: 0, pageCount: null,
      languages: ['eng'], explanationKeys: [],
    } as const;
    TestBed.configureTestingModule({
      providers: [
        DiscoveryStore,
        { provide: DiscoveryApi, useValue: { random: () => Promise.resolve(savedBook) } },
        {
          provide: CollectionsApi,
          useValue: { save: () => { saveCalls += 1; return pendingSave; } },
        },
      ],
    });
    const store = TestBed.inject(DiscoveryStore);
    await store.discover();
    const first = store.save();
    const duplicate = store.save();
    expect(saveCalls).toBe(1);
    finishSave?.();
    await Promise.all([first, duplicate]);
  });
});
