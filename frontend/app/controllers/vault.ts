import Controller from '@ember/controller';
import { action } from '@ember/object';
import { inject as service } from '@ember/service';
import type ApiService from '../services/api';
import type I18nService from '../services/i18n';

export default class VaultController extends Controller {
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
        
        // Extract externalBookId from the exported format (VaultEntryResponse)
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
}
