import { DESIGN_TOKENS } from './design-tokens';

describe('Editorial design tokens', () => {
  it('publishes the contract palette', () => {
    expect(DESIGN_TOKENS.color.paper).toBe('#FCFBF7');
    expect(DESIGN_TOKENS.color.ink).toBe('#171717');
    expect(DESIGN_TOKENS.color.cobalt).toBe('#0057B8');
  });

  it('keeps the foundation square and shadowless', () => {
    expect(DESIGN_TOKENS.radius.control).toBe('0');
    expect(DESIGN_TOKENS.depth.shadow).toBe('none');
  });
});
