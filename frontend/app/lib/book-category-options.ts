/**
 * Subject search terms for ISBNdb ({@code column=subjects}). Values stay in English
 * for API compatibility; labels use i18n keys {@code random.cat.*}.
 */
export const BOOK_CATEGORY_OPTIONS: ReadonlyArray<{ value: string; labelKey: string }> = [
  { value: 'Fiction', labelKey: 'random.cat.fiction' },
  { value: 'Nonfiction', labelKey: 'random.cat.nonfiction' },
  { value: 'Fantasy', labelKey: 'random.cat.fantasy' },
  { value: 'Science Fiction', labelKey: 'random.cat.scienceFiction' },
  { value: 'Mystery', labelKey: 'random.cat.mystery' },
  { value: 'Thriller', labelKey: 'random.cat.thriller' },
  { value: 'Romance', labelKey: 'random.cat.romance' },
  { value: 'Historical Fiction', labelKey: 'random.cat.historicalFiction' },
  { value: 'Biography', labelKey: 'random.cat.biography' },
  { value: 'History', labelKey: 'random.cat.history' },
  { value: 'Science', labelKey: 'random.cat.science' },
  { value: 'Poetry', labelKey: 'random.cat.poetry' },
  { value: 'Art', labelKey: 'random.cat.art' },
  { value: 'Philosophy', labelKey: 'random.cat.philosophy' },
  { value: 'Religion', labelKey: 'random.cat.religion' },
  { value: 'Psychology', labelKey: 'random.cat.psychology' },
  { value: 'Self-Help', labelKey: 'random.cat.selfHelp' },
  { value: 'Business', labelKey: 'random.cat.business' },
  { value: 'Children', labelKey: 'random.cat.children' },
  { value: 'Young Adult', labelKey: 'random.cat.youngAdult' },
  { value: 'Travel', labelKey: 'random.cat.travel' },
  { value: 'Cooking', labelKey: 'random.cat.cooking' },
];
