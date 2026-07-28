import { A11yModule } from '@angular/cdk/a11y';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TranslocoPipe } from '@jsverse/transloco';

import type { BookFilters } from '../../core/api/models';
import { DiscoveryStore } from './discovery.store';

@Component({
  selector: 'app-discover',
  imports: [A11yModule, FormsModule, TranslocoPipe],
  templateUrl: './discover.html',
  styleUrl: './discover.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Discover {
  protected readonly store = inject(DiscoveryStore);
  protected readonly filtersOpen = signal(false);
  protected language = '';
  protected subjects = '';
  protected yearFrom: number | null = null;
  protected yearTo: number | null = null;
  protected minimumRating: number | null = null;
  protected minimumRatingsCount: number | null = null;
  protected pagesFrom: number | null = null;
  protected pagesTo: number | null = null;

  protected applyFilters(): void {
    const filters: BookFilters = {
      ...(this.language ? { language: this.language } : {}),
      ...(this.subjects.trim() ? { subjects: this.subjects.split(',').map((value) => value.trim()).filter(Boolean) } : {}),
      ...(this.yearFrom === null ? {} : { publishedFrom: this.yearFrom }),
      ...(this.yearTo === null ? {} : { publishedTo: this.yearTo }),
      ...(this.minimumRating === null ? {} : { minimumRating: this.minimumRating }),
      ...(this.minimumRatingsCount === null ? {} : { minimumRatingsCount: this.minimumRatingsCount }),
      ...(this.pagesFrom === null ? {} : { minimumPages: this.pagesFrom }),
      ...(this.pagesTo === null ? {} : { maximumPages: this.pagesTo }),
    };
    this.store.updateFilters(filters);
    this.filtersOpen.set(false);
    void this.store.discover();
  }
}
