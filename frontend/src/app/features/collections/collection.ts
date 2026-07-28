import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TranslocoPipe } from '@jsverse/transloco';

import type { CollectionKind } from './collection.store';
import { CollectionStore } from './collection.store';

@Component({
  selector: 'app-collection',
  imports: [DatePipe, FormsModule, RouterLink, TranslocoPipe],
  providers: [CollectionStore],
  templateUrl: './collection.html',
  styleUrl: './collection.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Collection {
  protected readonly store = inject(CollectionStore);
  protected readonly kind: CollectionKind = inject(ActivatedRoute).snapshot.data['kind'] === 'history' ? 'history' : 'reading';
  protected readonly titleKey = this.kind === 'history' ? 'collection.history.title' : 'collection.reading.title';

  constructor() {
    void this.store.load(this.kind);
  }

  protected setQuery(value: string): void {
    this.store.query.set(value);
  }

  protected setSort(value: string): void {
    if (value === 'newest' || value === 'title') this.store.sort.set(value);
  }
}
