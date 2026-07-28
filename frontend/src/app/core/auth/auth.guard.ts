import { inject } from '@angular/core';
import { Router } from '@angular/router';
import type { CanActivateFn } from '@angular/router';

import { SessionService } from './session.service';

export const authGuard: CanActivateFn = async (_route, state) => {
  const session = inject(SessionService);
  const router = inject(Router);
  if (await session.ensureSession()) return true;
  return router.parseUrl(`/login?returnUrl=${encodeURIComponent(state.url)}`);
};
