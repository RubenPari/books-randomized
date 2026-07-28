import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';

import { SessionService } from './session.service';

describe('SessionService', () => {
  it('keeps the access token in memory and clears it on logout', async () => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    const session = TestBed.inject(SessionService);
    const http = TestBed.inject(HttpTestingController);

    const login = session.login({ email: 'reader@example.test', password: 'correct horse battery staple' });
    http.expectOne('/api/auth/login').flush({
      accessToken: 'jwt-in-memory',
      user: { id: 'user-1', email: 'reader@example.test' },
    });
    await login;

    expect(session.accessToken()).toBe('jwt-in-memory');

    const csrf = session.ensureCsrf();
    http.expectOne('/api/auth/csrf').flush({ token: 'csrf-token', headerName: 'X-XSRF-TOKEN' });
    await csrf;

    const logout = session.logout();
    await Promise.resolve();
    http.expectOne('/api/auth/logout').flush(null);
    await logout;
    expect(session.accessToken()).toBeNull();
    http.verify();
  });
});
