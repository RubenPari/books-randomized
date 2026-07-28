import { describe, expect, it } from 'vitest';

import { EN, IT } from './catalogs';

describe('translation catalogs', () => {
  it('keeps Italian and English keys in parity', () => {
    expect(Object.keys(IT).sort()).toEqual(Object.keys(EN).sort());
  });
});
