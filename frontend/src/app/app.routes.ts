import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { DiscoveryStore } from './features/discover/discovery.store';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/auth/auth-page').then(({ AuthPage }) => AuthPage),
    data: { mode: 'login' },
  },
  {
    path: 'register',
    loadComponent: () => import('./features/auth/auth-page').then(({ AuthPage }) => AuthPage),
    data: { mode: 'register' },
  },
  {
    path: 'forgot-password',
    loadComponent: () => import('./features/auth/auth-page').then(({ AuthPage }) => AuthPage),
    data: { mode: 'forgot' },
  },
  {
    path: 'reset-password',
    loadComponent: () => import('./features/auth/auth-page').then(({ AuthPage }) => AuthPage),
    data: { mode: 'reset' },
  },
  {
    path: 'discover',
    canActivate: [authGuard],
    providers: [DiscoveryStore],
    loadComponent: () => import('./features/discover/discover').then(({ Discover }) => Discover),
  },
  {
    path: 'reading-list',
    canActivate: [authGuard],
    loadComponent: () => import('./features/collections/collection').then(({ Collection }) => Collection),
    data: { kind: 'reading' },
  },
  {
    path: 'discovered',
    canActivate: [authGuard],
    loadComponent: () => import('./features/collections/collection').then(({ Collection }) => Collection),
    data: { kind: 'history' },
  },
  {
    path: 'profile',
    canActivate: [authGuard],
    loadComponent: () => import('./features/profile/profile').then(({ Profile }) => Profile),
  },
  {
    path: 'showcase',
    loadComponent: () => import('./showcase/showcase').then(({ Showcase }) => Showcase),
  },
  { path: '', pathMatch: 'full', redirectTo: 'discover' },
  { path: '**', redirectTo: 'discover' },
];
