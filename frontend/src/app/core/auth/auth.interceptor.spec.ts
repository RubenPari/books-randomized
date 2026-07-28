import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { authInterceptor } from './auth.interceptor';
import { SessionService } from './session.service';

describe('authInterceptor', () => {
  it('refreshes an expired session once and retries with the new bearer token', async () => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        {
          provide: SessionService,
          useValue: {
            accessToken: () => 'expired',
            refresh: () => Promise.resolve('fresh'),
            clear: () => undefined,
          },
        },
      ],
    });
    const client = TestBed.inject(HttpClient);
    const http = TestBed.inject(HttpTestingController);
    const result = firstValueFrom(client.get('/api/reading-list', { responseType: 'text' }));

    const expired = http.expectOne('/api/reading-list');
    expect(expired.request.headers.get('Authorization')).toBe('Bearer expired');
    expired.flush(null, { status: 401, statusText: 'Unauthorized' });

    await Promise.resolve();
    const retried = http.expectOne('/api/reading-list');
    expect(retried.request.headers.get('Authorization')).toBe('Bearer fresh');
    retried.flush('ok');
    expect(await result).toBe('ok');
  });
});
