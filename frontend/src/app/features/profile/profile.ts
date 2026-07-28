import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { TranslocoPipe } from '@jsverse/transloco';

import { AuthApi } from '../../core/api/api.clients';
import { SessionService } from '../../core/auth/session.service';
import { LocaleService } from '../../core/i18n/locale.service';

@Component({
  selector: 'app-profile',
  imports: [ReactiveFormsModule, TranslocoPipe],
  templateUrl: './profile.html',
  styleUrl: './profile.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Profile {
  private readonly api = inject(AuthApi);
  protected readonly session = inject(SessionService);
  protected readonly locale = inject(LocaleService);
  protected readonly pending = signal(false);
  protected readonly success = signal(false);
  protected readonly error = signal(false);
  protected readonly passwordForm = new FormGroup({
    currentPassword: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    newPassword: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(12)] }),
  });

  protected async changePassword(): Promise<void> {
    if (this.pending() || this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }
    this.pending.set(true);
    this.success.set(false);
    this.error.set(false);
    try {
      const value = this.passwordForm.getRawValue();
      await this.api.changePassword(value.currentPassword, value.newPassword);
      this.passwordForm.reset();
      this.success.set(true);
    } catch (error: unknown) {
      this.error.set(true);
    } finally {
      this.pending.set(false);
    }
  }

  protected setLocale(value: string): void {
    if (value === 'en' || value === 'it') this.locale.set(value);
  }
}
