import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';

import type {
  AuthResponse, Book, BookFilters, Credentials,
  DiscoveryResult, DiscoveredItem, Feedback, FeedbackValue, ReadingListItem, User,
} from './models';

@Injectable({ providedIn: 'root' })
export class AuthApi {
  private readonly http = inject(HttpClient);

  login(body: Credentials): Promise<AuthResponse> {
    return firstValueFrom(this.http.post<AuthResponse>('/api/auth/login', body, { withCredentials: true }));
  }

  register(body: Credentials): Promise<AuthResponse> {
    return firstValueFrom(this.http.post<AuthResponse>('/api/auth/register', body, { withCredentials: true }));
  }

  refresh(): Promise<AuthResponse> {
    return firstValueFrom(this.http.post<AuthResponse>('/api/auth/refresh', {}, { withCredentials: true }));
  }

  logout(): Promise<void> {
    return firstValueFrom(this.http.post<void>('/api/auth/logout', {}, { withCredentials: true }));
  }

  me(): Promise<User> {
    return firstValueFrom(this.http.get<User>('/api/auth/me'));
  }

  forgotPassword(email: string): Promise<void> {
    return firstValueFrom(this.http.post<void>('/api/auth/forgot-password', { email }));
  }

  resetPassword(token: string, newPassword: string): Promise<void> {
    return firstValueFrom(this.http.post<void>('/api/auth/reset-password', { token, newPassword }));
  }

  changePassword(currentPassword: string, newPassword: string): Promise<void> {
    return firstValueFrom(this.http.post<void>('/api/auth/change-password', { currentPassword, newPassword }));
  }

  csrf(): Promise<{ token: string; headerName: string }> {
    return firstValueFrom(this.http.get<{ token: string; headerName: string }>('/api/auth/csrf', { withCredentials: true }));
  }
}

@Injectable({ providedIn: 'root' })
export class DiscoveryApi {
  private readonly http = inject(HttpClient);

  random(filters: BookFilters): Promise<Book> {
    return firstValueFrom(this.http.post<DiscoveryResult>('/api/books/random', filters))
      .then(({ book, explanationKeys }) => ({
        workId: book.id,
        title: book.title,
        authors: book.authors,
        subjects: book.subjects,
        coverUrl: book.coverUrl,
        publicationYear: book.firstPublishedYear,
        rating: book.rating,
        ratingsCount: book.ratingsCount ?? 0,
        pageCount: book.pageCount,
        languages: book.languages,
        explanationKeys,
      }));
  }
}

@Injectable({ providedIn: 'root' })
export class CollectionsApi {
  private readonly http = inject(HttpClient);

  readingList(): Promise<readonly ReadingListItem[]> {
    return firstValueFrom(this.http.get<readonly ReadingListItem[]>('/api/reading-list'));
  }

  discovered(): Promise<readonly DiscoveredItem[]> {
    return firstValueFrom(this.http.get<readonly DiscoveredItem[]>('/api/discovered'));
  }

  save(catalogBookId: string, title: string, authors: readonly string[]): Promise<ReadingListItem> {
    return firstValueFrom(this.http.post<ReadingListItem>('/api/reading-list', {
      catalogBookId,
      status: 'WANT_TO_READ',
      title,
      authors,
    }));
  }

  remove(catalogBookId: string): Promise<void> {
    return firstValueFrom(this.http.delete<void>(`/api/reading-list/${encodeURIComponent(catalogBookId)}`));
  }

  removeDiscovered(catalogBookId: string): Promise<void> {
    return firstValueFrom(this.http.delete<void>(`/api/discovered/${encodeURIComponent(catalogBookId)}`));
  }

  feedback(catalogBookId: string, sentiment: FeedbackValue): Promise<Feedback> {
    return firstValueFrom(this.http.put<Feedback>('/api/feedback', {
      catalogBookId,
      sentiment,
      reason: null,
    }));
  }
}
