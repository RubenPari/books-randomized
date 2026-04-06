import Component from '@glimmer/component';
import { tracked } from '@glimmer/tracking';
import { action } from '@ember/object';
import { service } from '@ember/service';
import { on } from '@ember/modifier';
import t from '../helpers/t';
import type ApiService from '../services/api';
import type TranslationsService from '../services/translations';
import {
  vaultExportToImportEntries,
  VaultImportParseError,
} from '../lib/vault-import';
import type { VaultEntryResponse } from '../../types/api';

const VAULT_PARSE_I18N: Record<string, string> = {
  INVALID_STRUCTURE: 'vault.importErrorStructure',
  INVALID_ENTRY: 'vault.importErrorEntry',
  INVALID_BOOK: 'vault.importErrorBook',
  MISSING_EXTERNAL_ID: 'vault.importErrorExternalId',
};

interface Signature {
  Args: {
    model: VaultEntryResponse[];
  };
}

export default class VaultPage extends Component<Signature> {
  @service declare api: ApiService;
  @service declare translations: TranslationsService;

  @tracked exportError: string | null = null;
  @tracked importError: string | null = null;

  @action
  async exportVault() {
    this.exportError = null;
    try {
      const data = await this.api.listVault();
      const blob = new Blob([JSON.stringify(data, null, 2)], {
        type: 'application/json',
      });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `vault-export-${new Date().toISOString().split('T')[0]}.json`;
      a.click();
      URL.revokeObjectURL(url);
    } catch (e: unknown) {
      this.exportError =
        e instanceof Error
          ? e.message
          : this.translations.t('vault.exportFailed');
    }
  }

  @action
  importVault(event: Event) {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }

    this.importError = null;

    const reader = new FileReader();
    reader.onload = async (e) => {
      try {
        const content = e.target?.result as string;
        let data: unknown;
        try {
          data = JSON.parse(content);
        } catch {
          this.importError = this.translations.t('vault.importJsonInvalid');
          input.value = '';
          return;
        }

        const entries = vaultExportToImportEntries(data);
        await this.api.importVault(entries);
        input.value = '';
        window.location.reload();
      } catch (err: unknown) {
        if (err instanceof VaultImportParseError) {
          const key = VAULT_PARSE_I18N[err.code];
          this.importError = key
            ? this.translations.t(key)
            : this.translations.t('vault.importFailed');
        } else {
          this.importError =
            err instanceof Error
              ? err.message
              : this.translations.t('vault.importFailed');
        }
        input.value = '';
      }
    };
    reader.readAsText(file);
  }

  <template>
    <section class="page">
      <header class="page-header">
        <h2>{{t "vault.title"}}</h2>
        <p>{{t "vault.description"}}</p>
        <div class="actions">
          <button
            type="button"
            class="secondary"
            {{on "click" this.exportVault}}
          >
            {{t "vault.export"}}
          </button>
          <label class="button secondary">
            {{t "vault.import"}}
            <input
              type="file"
              accept=".json"
              hidden
              {{on "change" this.importVault}}
            />
          </label>
        </div>
        {{#if this.exportError}}
          <p class="error vault-banner">{{this.exportError}}</p>
        {{/if}}
        {{#if this.importError}}
          <p class="error vault-banner">{{this.importError}}</p>
        {{/if}}
      </header>

      {{#if @model.length}}
        <div class="list">
          {{#each @model as |entry|}}
            <div class="card list-item">
              <h3>{{entry.book.title}}</h3>
              {{#if entry.book.authors}}
                <p class="meta">
                  {{#each entry.book.authors as |author i|}}
                    {{#if i}} · {{/if}}{{author}}
                  {{/each}}
                </p>
              {{/if}}
              {{#if entry.note}}
                <p>{{entry.note}}</p>
              {{/if}}
              {{#if entry.personalRating}}
                <p>{{t "vault.rating"}}: {{entry.personalRating}}</p>
              {{/if}}
            </div>
          {{/each}}
        </div>
      {{else}}
        <div class="card empty">
          <p>{{t "vault.empty"}}</p>
        </div>
      {{/if}}
    </section>
  </template>
}
