import { TestBed } from '@angular/core/testing';

import { Discover } from './discover';
import { DiscoveryStore } from './discovery.store';
import { appConfig } from '../../app.config';

describe('Discover', () => {
  it('renders an actionable offline state without reporting false success', async () => {
    await TestBed.configureTestingModule({
      imports: [Discover],
      providers: [...(appConfig.providers ?? []), {
        provide: DiscoveryStore,
        useValue: {
          status: () => 'offline', book: () => null, filters: () => ({}),
          hasResult: () => false,
          discover: () => Promise.resolve(), updateFilters: () => undefined,
        },
      }],
    }).compileComponents();
    const fixture = TestBed.createComponent(Discover);
    fixture.detectChanges();
    expect((fixture.nativeElement as HTMLElement).querySelector('[data-state="offline"]')).not.toBeNull();
    expect((fixture.nativeElement as HTMLElement).querySelector('button')).not.toBeNull();
  });
});
