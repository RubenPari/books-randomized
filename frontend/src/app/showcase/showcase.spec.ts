import { TestBed } from '@angular/core/testing';
import { Showcase } from './showcase';

describe('Showcase', () => {
  it('renders every required primitive and feedback state', async () => {
    await TestBed.configureTestingModule({ imports: [Showcase] }).compileComponents();
    const fixture = TestBed.createComponent(Showcase);
    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelectorAll('[data-primitive="button"]')).toHaveLength(3);
    expect(element.querySelector('[data-primitive="field"]')).not.toBeNull();
    expect(element.querySelector('[data-primitive="chip"]')).not.toBeNull();
    expect(element.querySelector('[data-primitive="book-tile"]')).not.toBeNull();
    expect(element.querySelector('[data-state="loading"]')).not.toBeNull();
    expect(element.querySelector('[data-state="empty"]')).not.toBeNull();
    expect(element.querySelector('[data-state="error"]')).not.toBeNull();
  });

  it('opens and closes the drawer without losing an accessible name', async () => {
    await TestBed.configureTestingModule({ imports: [Showcase] }).compileComponents();
    const fixture = TestBed.createComponent(Showcase);
    fixture.detectChanges();

    const openButton = fixture.nativeElement.querySelector('[data-open-drawer]') as HTMLButtonElement;
    openButton.click();
    fixture.detectChanges();

    const drawer = fixture.nativeElement.querySelector('[role="dialog"]') as HTMLElement;
    expect(drawer.getAttribute('aria-labelledby')).toBe('drawer-title');

    const closeButton = drawer.querySelector('button') as HTMLButtonElement;
    closeButton.click();
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[role="dialog"]')).toBeNull();
  });
});
