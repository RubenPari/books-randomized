import type { BookResponse } from './api';

/** Minimal book fields stored in session discovery history (sessionStorage). */
export type BookSummary = Pick<
  BookResponse,
  'id' | 'externalId' | 'title' | 'description' | 'coverUrl' | 'authors'
>;
