import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { describe, expect, it } from 'vitest';

import { AuthApi, CollectionsApi, DiscoveryApi } from './api.clients';

describe('API client contracts', () => {
  it('sends the password-reset body expected by the API', async () => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    const api = TestBed.inject(AuthApi);
    const http = TestBed.inject(HttpTestingController);

    const reset = api.resetPassword('reset-token', 'replacement password');

    const request = http.expectOne('/api/auth/reset-password');
    expect(request.request.body).toEqual({ token: 'reset-token', newPassword: 'replacement password' });
    request.flush(null);
    await reset;
  });

  it('normalizes the nested discovery result while preserving its explanation keys', async () => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    const api = TestBed.inject(DiscoveryApi);
    const http = TestBed.inject(HttpTestingController);

    const random = api.random({ publishedFrom: 1960, maximumPages: 500 });

    const request = http.expectOne('/api/books/random');
    expect(request.request.body).toEqual({ publishedFrom: 1960, maximumPages: 500 });
    request.flush({
      book: {
        id: 'OL45883W', title: 'The Left Hand of Darkness', authors: ['Ursula K. Le Guin'],
        firstPublishedYear: 1969, coverUrl: null, subjects: ['Science Fiction'], languages: ['eng'],
        rating: 4.3, ratingsCount: 8241, pageCount: 304,
      },
      explanationKeys: ['discovery.explanation.filters'],
    });

    await expect(random).resolves.toMatchObject({
      workId: 'OL45883W', publicationYear: 1969,
      explanationKeys: ['discovery.explanation.filters'],
    });
  });

  it('uses catalog-book identifiers for reading-list save, delete, and feedback', async () => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    const api = TestBed.inject(CollectionsApi);
    const http = TestBed.inject(HttpTestingController);

    const save = api.save('OL45883W', 'The Left Hand of Darkness', ['Ursula K. Le Guin']);
    const saveRequest = http.expectOne('/api/reading-list');
    expect(saveRequest.request.body).toEqual({
      catalogBookId: 'OL45883W',
      status: 'WANT_TO_READ',
      title: 'The Left Hand of Darkness',
      authors: ['Ursula K. Le Guin'],
    });
    saveRequest.flush({
      id: 'reading-1', catalogBookId: 'OL45883W', status: 'WANT_TO_READ', addedAt: '2026-07-27T12:00:00Z',
      title: 'The Left Hand of Darkness', authors: ['Ursula K. Le Guin'],
    });
    await save;

    const remove = api.remove('OL45883W');
    const removeRequest = http.expectOne('/api/reading-list/OL45883W');
    expect(removeRequest.request.method).toBe('DELETE');
    removeRequest.flush(null);
    await remove;

    const feedback = api.feedback('OL45883W', 'LIKE');
    const feedbackRequest = http.expectOne('/api/feedback');
    expect(feedbackRequest.request.method).toBe('PUT');
    expect(feedbackRequest.request.body).toEqual({ catalogBookId: 'OL45883W', sentiment: 'LIKE', reason: null });
    feedbackRequest.flush({
      id: 'feedback-1', catalogBookId: 'OL45883W', sentiment: 'LIKE', reason: null,
      createdAt: '2026-07-27T12:00:00Z',
    });
    await feedback;
  });
});
