/** Mirrors backend {@code BookResponse} JSON shape. */
export interface BookResponse {
  id: string;
  externalId: string;
  title: string;
  authors?: string[];
  categories?: string[];
  language?: string | null;
  rating?: number | null;
  publicationYear?: number | null;
  description?: string | null;
  coverUrl?: string | null;
}

export interface VaultEntryResponse {
  id: string;
  book: BookResponse;
  note?: string | null;
  personalRating?: number | null;
  createdAt: string;
}

export interface DiscoveryResponse {
  id: string;
  book: BookResponse;
  sessionId: string;
  discoveredAt: string;
}

export interface StatsResponse {
  totalDiscovered: number;
  totalVaulted: number;
  averageRating: number;
  byCategory: Record<string, number>;
  byAuthor: Record<string, number>;
  byLanguage: Record<string, number>;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
}

export interface VaultImportEntryPayload {
  externalBookId: string;
  note?: string;
  personalRating?: number;
}
