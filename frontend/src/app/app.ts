import { A11yModule } from '@angular/cdk/a11y';
import { ChangeDetectionStrategy, Component, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { TranslocoPipe } from '@jsverse/transloco';
import { inject } from '@angular/core';

import { SessionService } from './core/auth/session.service';
import { LocaleService } from './core/i18n/locale.service';

@Component({
  selector: 'app-root',
  imports: [A11yModule, RouterLink, RouterLinkActive, RouterOutlet, TranslocoPipe],
  templateUrl: './app.html',
  styleUrl: './app.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class App {
  protected readonly session = inject(SessionService);
  protected readonly locale = inject(LocaleService);
  private readonly router = inject(Router);
  protected readonly navigationOpen = signal(false);

  protected closeNavigation(): void {
    this.navigationOpen.set(false);
  }

  protected openNavigation(): void {
    this.navigationOpen.set(true);
  }

  protected logout(): void {
    void this.session.logout().then(() => this.router.navigateByUrl('/login'));
  }

  protected setLocale(value: string): void {
    if (value === 'en' || value === 'it') this.locale.set(value);
  }
}
