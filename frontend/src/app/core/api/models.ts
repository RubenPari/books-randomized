export type User = {
  readonly id: string;
  readonly email: string;
};

export type Locale = 'en' | 'it';

export type AuthResponse = {
  readonly accessToken: string;
  readonly user: User;
};

export type Credentials = {
  readonly email: string;
  readonly password: string;
};

export type Book = {
  readonly workId: string;
  readonly title: string;
  readonly authors: readonly string[];
  readonly subjects: readonly string[];
  readonly coverUrl: string | null;
  readonly publicationYear: number | null;
  readonly rating: number | null;
  readonly ratingsCount: number;
  readonly pageCount: number | null;
  readonly languages: readonly string[];
  readonly explanationKeys: readonly string[];
};

export type BookFilters = {
  readonly language?: string;
  readonly subjects?: readonly string[];
  readonly publishedFrom?: number;
  readonly publishedTo?: number;
  readonly minimumRating?: number;
  readonly minimumRatingsCount?: number;
  readonly minimumPages?: number;
  readonly maximumPages?: number;
};

export type ReadingStatus = 'WANT_TO_READ';

export type ReadingListItem = {
  readonly id: string;
  readonly catalogBookId: string;
  readonly status: ReadingStatus;
  readonly addedAt: string;
  readonly title: string;
  readonly authors: readonly string[];
};

export type DiscoveredItem = {
  readonly id: string;
  readonly catalogBookId: string;
  readonly discoveredAt: string;
  readonly title: string;
  readonly authors: readonly string[];
};

export type CollectionItem = ReadingListItem | DiscoveredItem;

export type FeedbackValue = 'LIKE' | 'DISLIKE';

export type Feedback = {
  readonly id: string;
  readonly catalogBookId: string;
  readonly sentiment: FeedbackValue;
  readonly reason: string | null;
  readonly createdAt: string;
};

export type DiscoveryResult = {
  readonly book: {
    readonly id: string;
    readonly title: string;
    readonly authors: readonly string[];
    readonly firstPublishedYear: number | null;
    readonly coverUrl: string | null;
    readonly subjects: readonly string[];
    readonly languages: readonly string[];
    readonly rating: number | null;
    readonly ratingsCount: number | null;
    readonly pageCount: number | null;
  };
  readonly explanationKeys: readonly string[];
};

export type ApiProblem = {
  readonly type?: string;
  readonly title: string;
  readonly status: number;
  readonly detail?: string;
};
