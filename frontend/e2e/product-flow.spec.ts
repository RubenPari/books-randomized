import { expect, test } from '@playwright/test';
import type { Page, Route } from '@playwright/test';

const user = {
  id: 'user-1',
  email: 'reader@example.test',
} as const;

const catalogBook = {
  id: 'OL45883W',
  title: 'The Left Hand of Darkness',
  authors: ['Ursula K. Le Guin'],
  firstPublishedYear: 1969,
  coverUrl: null,
  subjects: ['Science Fiction', 'Identity'],
  languages: ['eng'],
  rating: 4.3,
  ratingsCount: 8241,
  pageCount: 304,
} as const;

const discoveryResult = {
  book: catalogBook,
  explanationKeys: ['discovery.explanation.filters'],
} as const;

const bookTitle = catalogBook.title;

async function installApi(page: Page): Promise<void> {
  let saved = false;
  await page.route('**/api/**', async (route: Route) => {
    const request = route.request();
    const path = new URL(request.url()).pathname;
    if (path === '/api/auth/csrf') {
      await route.fulfill({ json: { token: 'test-csrf', headerName: 'X-XSRF-TOKEN' } });
    } else if (path === '/api/auth/register' || path === '/api/auth/login' || path === '/api/auth/refresh') {
      await route.fulfill({ json: { accessToken: 'test-jwt', user } });
    } else if (path === '/api/auth/logout') {
      await route.fulfill({ status: 204 });
    } else if (path === '/api/books/random') {
      await route.fulfill({ json: discoveryResult });
    } else if (path === '/api/reading-list' && request.method() === 'POST') {
      saved = true;
      await route.fulfill({
        json: {
          id: 'saved-1',
          catalogBookId: catalogBook.id,
          status: 'WANT_TO_READ',
          addedAt: '2026-07-26T12:00:00Z',
          title: catalogBook.title,
          authors: catalogBook.authors,
        },
      });
    } else if (path === '/api/reading-list' && request.method() === 'GET') {
      await route.fulfill({
        json: saved
          ? [{
              id: 'saved-1',
              catalogBookId: catalogBook.id,
              status: 'WANT_TO_READ',
              addedAt: '2026-07-26T12:00:00Z',
              title: catalogBook.title,
              authors: catalogBook.authors,
            }]
          : [],
      });
    } else if (path === `/api/reading-list/${catalogBook.id}`) {
      saved = false;
      await route.fulfill({ status: 204 });
    } else if (path === '/api/discovered' && request.method() === 'GET') {
      await route.fulfill({
        json: [{
          id: 'history-1',
          catalogBookId: catalogBook.id,
          discoveredAt: '2026-07-26T12:00:00Z',
          title: catalogBook.title,
          authors: catalogBook.authors,
        }],
      });
    } else if (path === `/api/discovered/${catalogBook.id}` && request.method() === 'DELETE') {
      await route.fulfill({ status: 204 });
    } else if (path === '/api/feedback' && request.method() === 'PUT') {
      await route.fulfill({
        json: {
          id: 'feedback-1',
          catalogBookId: catalogBook.id,
          sentiment: 'LIKE',
          reason: null,
          createdAt: '2026-07-26T12:00:00Z',
        },
      });
    } else {
      await route.fulfill({
        status: 404,
        contentType: 'application/problem+json',
        body: JSON.stringify({ title: 'Not found', status: 404 }),
      });
    }
  });
}

async function settleAtOrigin(page: Page): Promise<void> {
  await page.evaluate(async () => {
    scrollTo(0, 0);
    await document.fonts.ready;
  });
}

test('register, filter, discover, save, feedback, collections, locale and logout', async ({ page }) => {
  await installApi(page);
  await page.goto('/register');
  await page.getByLabel('Email').fill('reader@example.test');
  await page.getByLabel('Password').fill('correct horse battery staple');
  await page.screenshot({ path: 'evidence/task-4-ui-flow/register-form.png', fullPage: true });
  await page.getByRole('button', { name: 'Create account' }).click();
  await expect(page).toHaveURL(/\/discover$/);

  await page.getByRole('button', { name: 'Filters' }).click();
  await page.screenshot({ path: 'evidence/task-4-ui-flow/filter-drawer-keyboard.png', fullPage: true });
  await page.getByLabel('Subjects, separated by commas').fill('science fiction, identity');
  await page.getByLabel('Minimum rating', { exact: true }).fill('4');
  await page.getByRole('button', { name: 'Draw a book' }).last().click();
  await expect(page.getByRole('heading', { name: bookTitle })).toBeVisible();
  await expect(page.getByText('Matched your filters.')).toBeVisible();
  await page.getByRole('button', { name: 'Save to reading list' }).click();
  await expect(page.getByRole('button', { name: 'Saved' })).toBeDisabled();
  await page.getByRole('button', { name: 'More like this' }).click();
  await expect(page.getByRole('button', { name: 'More like this' })).toHaveAttribute('aria-pressed', 'true');

  await page.goto('/reading-list');
  await expect(page.getByRole('heading', { name: bookTitle })).toBeVisible();
  await page.screenshot({ path: 'evidence/task-4-ui-flow/reading-list.png', fullPage: true });
  await page.getByLabel('Search titles or authors').fill('Left Hand');
  await expect(page.getByRole('heading', { name: bookTitle })).toBeVisible();
  await page.getByRole('button', { name: 'Remove' }).click();
  await expect(page.getByText('There are no books here yet.')).toBeVisible();
  await page.screenshot({ path: 'evidence/task-4-ui-flow/reading-list-empty.png', fullPage: true });

  await page.goto('/discovered');
  await expect(page.getByRole('heading', { name: bookTitle })).toBeVisible();
  await page.getByLabel('Language').selectOption('it');
  await expect(page.getByRole('link', { name: 'Scopri' })).toBeVisible();
  await page.screenshot({ path: 'evidence/task-4-ui-flow/discovered-italian.png', fullPage: true });
  await page.reload();
  await expect(page.locator('html')).toHaveAttribute('lang', 'it');
  await page.getByRole('button', { name: 'Esci' }).click();
  await expect(page).toHaveURL(/\/login$/);
  await expect(page.getByRole('link', { name: 'Accedi' })).toBeVisible();
});

for (const viewport of [
  { name: 'mobile', width: 375, height: 900 },
  { name: 'tablet', width: 768, height: 1024 },
  { name: 'desktop', width: 1280, height: 900 },
] as const) {
  test(`discovery is usable at ${viewport.width}px`, async ({ page }) => {
    await installApi(page);
    await page.setViewportSize(viewport);
    await page.goto('/discover');
    await expect(page.getByRole('heading', { name: /Find the book/ })).toBeVisible();
    await page.getByRole('button', { name: 'Draw a book' }).click();
    await expect(page.getByRole('heading', { name: bookTitle })).toBeVisible();
    expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true);
    await page.screenshot({ path: `evidence/task-4-ui-flow/discover-${viewport.name}-${viewport.width}.png`, fullPage: true });
  });
}

test('guard refreshes an expired in-memory session on a direct protected route', async ({ page }) => {
  await installApi(page);
  await page.goto('/reading-list');
  await expect(page).toHaveURL(/\/reading-list$/);
  await expect(page.getByRole('heading', { name: 'Your reading list' })).toBeVisible();
});

test('offline discovery reports an actionable state and reduced motion is honored', async ({ page, context }) => {
  await installApi(page);
  await page.goto('/discover');
  await context.setOffline(true);
  await page.route('**/api/books/random', async (route) => route.abort('internetdisconnected'));
  await page.getByRole('button', { name: 'Draw a book' }).click();
  await expect(page.getByText('You appear to be offline.')).toBeVisible();
  await page.screenshot({ path: 'evidence/task-4-ui-flow/discover-offline.png', fullPage: true });
  await context.setOffline(false);
  await page.emulateMedia({ reducedMotion: 'reduce' });
  expect(await page.getByRole('button', { name: 'Try again' }).evaluate((element) => getComputedStyle(element).transitionDuration)).toBe('0s');
});

test('malformed problem responses and unbroken titles stay honest and usable', async ({ page }) => {
  await installApi(page);
  await page.goto('/discover');
  await page.route('**/api/books/random', async (route) => route.fulfill({
    status: 422,
    contentType: 'application/problem+json',
    body: '{"title":"Malformed filter","status":422',
  }));
  await page.getByRole('button', { name: 'Draw a book' }).click();
  await expect(page.getByText('The shelf could not load.')).toBeVisible();
  await settleAtOrigin(page);
  await page.screenshot({ path: 'evidence/task-4-ui-flow/discover-problem-error.png', fullPage: true });
  await page.unroute('**/api/books/random');
  await page.route('**/api/books/random', async (route) => route.fulfill({
    json: {
      book: {
        ...catalogBook,
        title: 'Thisisanextraordinarilylongunbrokenbooktitlethatmustremainfullyusableandvisible',
      },
      explanationKeys: ['discovery.explanation.filters'],
    },
  }));
  await page.setViewportSize({ width: 375, height: 900 });
  await page.getByRole('button', { name: 'Try again' }).click();
  const longHeading = page.getByRole('heading', { name: 'Thisisanextraordinarilylongunbrokenbooktitlethatmustremainfullyusableandvisible' });
  await expect(longHeading).toHaveText('Thisisanextraordinarilylongunbrokenbooktitlethatmustremainfullyusableandvisible');
  await expect(page.getByRole('button', { name: 'Save to reading list' })).toBeVisible();
  await expect(page.getByText('Book data provided by Open Library.')).toBeVisible();
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true);
  await page.evaluate(() => scrollTo(0, 0));
  await page.screenshot({ path: 'evidence/task-4-ui-flow/discover-long-title-375.png', fullPage: true });
});

test('filter drawer is keyboard reachable and Escape restores the page', async ({ page }) => {
  await installApi(page);
  await page.goto('/discover');
  await page.getByRole('button', { name: 'Filters' }).focus();
  await page.keyboard.press('Enter');
  const drawer = page.getByRole('dialog', { name: 'Filters' });
  await expect(drawer).toBeVisible();
  await expect(page.getByRole('button', { name: 'Close filters' })).toBeFocused();
  await page.keyboard.press('Escape');
  await expect(drawer).toBeHidden();
  await expect(page.getByRole('button', { name: 'Filters' })).toBeFocused();
});

test('every auth and account route renders its form at mobile width', async ({ page }) => {
  await installApi(page);
  await page.setViewportSize({ width: 375, height: 900 });
  for (const target of [
    { route: '/login', heading: 'Return to your shelf', artifact: 'login-375.png' },
    { route: '/forgot-password', heading: 'Reset your password', artifact: 'forgot-password-375.png' },
    { route: '/reset-password?token=test-token', heading: 'Choose a new password', artifact: 'reset-password-375.png' },
    { route: '/profile', heading: 'Your account', artifact: 'profile-375.png' },
  ] as const) {
    await page.goto(target.route);
    await expect(page.getByRole('heading', { name: target.heading })).toBeVisible();
    await settleAtOrigin(page);
    await page.screenshot({ path: `evidence/task-4-ui-flow/${target.artifact}`, fullPage: true });
  }
});

test('dark masthead exposes a high-contrast keyboard focus ring', async ({ page }) => {
  await installApi(page);
  await page.goto('/login');
  await page.getByRole('link', { name: 'Books, Randomized' }).focus();
  await expect(page.getByRole('link', { name: 'Books, Randomized' })).toBeFocused();
  await page.screenshot({ path: 'evidence/task-4-ui-flow/masthead-keyboard-focus.png', fullPage: true });
});
