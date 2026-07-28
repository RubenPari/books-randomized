import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { describe, expect, it, vi } from 'vitest';

import { authGuard } from './auth.guard';
import { SessionService } from './session.service';

describe('authGuard', () => {
  it('redirects an anonymous visitor to login with the return URL', async () => {
    const parseUrl = vi.fn((url: string) => url);
    TestBed.configureTestingModule({
      providers: [
        { provide: SessionService, useValue: { ensureSession: () => Promise.resolve(false) } },
        { provide: Router, useValue: { parseUrl } },
      ],
    });

    const result = await TestBed.runInInjectionContext(() =>
      authGuard({} as never, { url: '/reading-list' } as never),
    );
    expect(result).toBe('/login?returnUrl=%2Freading-list');
  });
});
