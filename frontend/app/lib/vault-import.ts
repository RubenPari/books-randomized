import type { VaultImportEntryPayload } from '../../types/api';

export const VAULT_IMPORT_ERR = {
  INVALID_STRUCTURE: 'INVALID_STRUCTURE',
  INVALID_ENTRY: 'INVALID_ENTRY',
  INVALID_BOOK: 'INVALID_BOOK',
  MISSING_EXTERNAL_ID: 'MISSING_EXTERNAL_ID',
} as const;

export type VaultImportErrorCode =
  (typeof VAULT_IMPORT_ERR)[keyof typeof VAULT_IMPORT_ERR];

export class VaultImportParseError extends Error {
  readonly code: VaultImportErrorCode;

  constructor(code: VaultImportErrorCode) {
    super(code);
    this.code = code;
  }
}

/**
 * Parses a vault export JSON file (array of vault entries) into import payloads.
 */
export function vaultExportToImportEntries(
  data: unknown
): VaultImportEntryPayload[] {
  if (!Array.isArray(data)) {
    throw new VaultImportParseError(VAULT_IMPORT_ERR.INVALID_STRUCTURE);
  }
  return data.map((entry) => {
    if (typeof entry !== 'object' || entry === null) {
      throw new VaultImportParseError(VAULT_IMPORT_ERR.INVALID_ENTRY);
    }
    const e = entry as Record<string, unknown>;
    const book = e.book;
    if (typeof book !== 'object' || book === null) {
      throw new VaultImportParseError(VAULT_IMPORT_ERR.INVALID_BOOK);
    }
    const externalId = (book as Record<string, unknown>).externalId;
    if (typeof externalId !== 'string' || !externalId) {
      throw new VaultImportParseError(VAULT_IMPORT_ERR.MISSING_EXTERNAL_ID);
    }
    const note = e.note;
    const personalRating = e.personalRating;
    return {
      externalBookId: externalId,
      ...(typeof note === 'string' ? { note } : {}),
      ...(typeof personalRating === 'number' ? { personalRating } : {}),
    };
  });
}
