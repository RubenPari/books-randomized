import { expect, test } from '@playwright/test';

const viewports = [
  { name: 'mobile', width: 375, height: 900 },
  { name: 'tablet', width: 768, height: 1024 },
  { name: 'desktop', width: 1280, height: 900 },
] as const;

for (const viewport of viewports) {
  test(`showcase reflows at ${viewport.width}px`, async ({ page }) => {
    await page.setViewportSize(viewport);
    await page.goto('/showcase');
    await expect(page.getByRole('heading', { name: 'The component index' })).toBeVisible();
    const overflow = await page.evaluate(() => document.documentElement.scrollWidth > window.innerWidth);
    expect(overflow).toBe(false);
    await page.screenshot({
      path: `evidence/showcase-${viewport.name}-${viewport.width}.png`,
      fullPage: true,
    });
  });
}

test('drawer is keyboard reachable and Escape dismisses it', async ({ page }) => {
  await page.goto('/showcase');
  await page.getByRole('button', { name: 'Open drawer' }).focus();
  await page.keyboard.press('Enter');
  const drawer = page.getByRole('dialog', { name: 'Browse the issue' });
  await expect(drawer).toBeVisible();
  await expect(page.getByRole('button', { name: 'Close drawer' })).toBeFocused();
  await page.screenshot({ path: 'evidence/showcase-drawer-open.png', fullPage: true });
  await page.keyboard.press('Escape');
  await expect(drawer).toBeHidden();
});

test('dialog is named, focused, and visibly documented', async ({ page }) => {
  await page.goto('/showcase');
  await page.getByRole('button', { name: 'Open dialog' }).click();
  const dialog = page.getByRole('dialog', { name: 'Draw another shelf?' });
  await expect(dialog).toBeVisible();
  await expect(page.getByRole('button', { name: 'Keep these' })).toBeFocused();
  await page.screenshot({ path: 'evidence/showcase-dialog-open.png', fullPage: true });
});

test('mobile masthead navigation opens and dismisses with Escape', async ({ page }) => {
  await page.setViewportSize({ width: 375, height: 900 });
  await page.goto('/showcase');
  await page.getByRole('button', { name: 'Menu' }).click();
  const navigation = page.getByRole('dialog', { name: 'Books, Randomized' });
  await expect(navigation).toBeVisible();
  await expect(page.getByRole('button', { name: 'Close' })).toBeFocused();
  await page.screenshot({ path: 'evidence/showcase-mobile-navigation-open.png', fullPage: true });
  await page.keyboard.press('Escape');
  await expect(navigation).toBeHidden();
});

test('reduced motion removes meaningful transition duration', async ({ browser }) => {
  const context = await browser.newContext({ reducedMotion: 'reduce' });
  const page = await context.newPage();
  await page.goto('/showcase');
  const duration = await page.getByRole('button', { name: 'Randomize' }).evaluate(
    (button) => getComputedStyle(button).transitionDuration,
  );
  expect(duration).toBe('0s');
  await context.close();
});
