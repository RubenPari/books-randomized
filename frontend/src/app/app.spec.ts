import { TestBed } from '@angular/core/testing';
import { appConfig } from './app.config';
import { App } from './app';

describe('App shell', () => {
  it('offers anonymous readers a localized sign-in route', async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: appConfig.providers ?? [],
    }).compileComponents();

    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelector('nav a[href="/login"]')?.textContent).toContain('Sign in');
    expect(element.querySelector('main')).not.toBeNull();
  });
});
