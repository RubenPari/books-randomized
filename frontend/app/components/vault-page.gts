import Component from '@glimmer/component';
import { action } from '@ember/object';
import { service } from '@ember/service';
import { on } from '@ember/modifier';
import t from '../helpers/t';
import type ApiService from '../services/api';
import type I18nService from '../services/i18n';

interface VaultEntry {
  book: {
    title: string;
    authors?: string;
    externalId: string;
  };
  note?: string;
  personalRating?: number;
}

interface Signature {
  Args: {
    model: VaultEntry[];
  };
}

export default class VaultPage extends Component<Signature> {
  @service declare api: ApiService;
  @service declare i18n: I18nService;

  @action
  async exportVault() {
    try {
      const data = await this.api.listVault();
      const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `vault-export-${new Date().toISOString().split('T')[0]}.json`;
      a.click();
      URL.revokeObjectURL(url);
    } catch (e: any) {
      alert(e.message);
    }
  }

  @action
  async importVault(event: Event) {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = async (e) => {
      try {
        const content = e.target?.result as string;
        const data = JSON.parse(content);

        const entries = data.map((entry: any) => ({
          externalBookId: entry.book.externalId,
          note: entry.note,
          personalRating: entry.personalRating
        }));

        await this.api.importVault(entries);
        alert(this.i18n.locale === 'it' ? 'Importazione completata!' : 'Import completed!');
        window.location.reload();
      } catch (err: any) {
        alert('Invalid JSON file: ' + err.message);
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
          <button type="button" class="secondary" {{on "click" this.exportVault}}>
            {{t "vault.export"}}
          </button>
          <label class="button secondary">
            {{t "vault.import"}}
            <input type="file" accept=".json" hidden {{on "change" this.importVault}} />
          </label>
        </div>
      </header>

      {{#if @model.length}}
        <div class="list">
          {{#each @model as |entry|}}
            <div class="card list-item">
              <h3>{{entry.book.title}}</h3>
              <p class="meta">{{entry.book.authors}}</p>
              {{#if entry.note}}
                <p>{{entry.note}}</p>
              {{/if}}
              {{#if entry.personalRating}}
                <p>Rating: {{entry.personalRating}}</p>
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
