import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TranslocoPipe } from '@jsverse/transloco';

import { AuthApi } from '../../core/api/api.clients';
import { SessionService } from '../../core/auth/session.service';

type AuthMode = 'login' | 'register' | 'forgot' | 'reset';

@Component({
  selector: 'app-auth-page',
  imports: [ReactiveFormsModule, RouterLink, TranslocoPipe],
  templateUrl: './auth-page.html',
  styleUrl: './auth-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AuthPage {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly session = inject(SessionService);
  private readonly api = inject(AuthApi);
  protected readonly mode: AuthMode = this.readMode();
  protected readonly pending = signal(false);
  protected readonly error = signal(false);
  protected readonly success = signal(false);
  protected readonly form = new FormGroup({
    email: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email] }),
    password: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(12)] }),
  });

  constructor() {
    if (this.mode === 'forgot') this.form.controls.password.clearValidators();
    if (this.mode === 'reset') this.form.controls.email.clearValidators();
    this.form.updateValueAndValidity();
  }

  protected async submit(): Promise<void> {
    if (this.pending() || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.pending.set(true);
    this.error.set(false);
    try {
      const value = this.form.getRawValue();
      if (this.mode === 'login') {
        await this.session.login({ email: value.email, password: value.password });
        await this.navigateAfterAuth();
      } else if (this.mode === 'register') {
        await this.session.register(value);
        await this.navigateAfterAuth();
      } else if (this.mode === 'forgot') {
        await this.api.forgotPassword(value.email);
        this.success.set(true);
      } else {
        await this.api.resetPassword(this.route.snapshot.queryParamMap.get('token') ?? '', value.password);
        this.success.set(true);
      }
    } catch (error: unknown) {
      this.error.set(true);
    } finally {
      this.pending.set(false);
    }
  }

  private readMode(): AuthMode {
    const mode = this.route.snapshot.data['mode'];
    if (mode === 'register' || mode === 'forgot' || mode === 'reset') return mode;
    return 'login';
  }

  private navigateAfterAuth(): Promise<boolean> {
    const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
    return this.router.navigateByUrl(returnUrl?.startsWith('/') ? returnUrl : '/discover');
  }
}
