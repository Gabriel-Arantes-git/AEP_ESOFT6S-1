import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { gestorGuard } from './core/guards/gestor.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login').then(m => m.LoginComponent)
  },
  {
    path: 'cadastro',
    loadComponent: () => import('./features/auth/cadastro/cadastro').then(m => m.CadastroComponent)
  },
  {
    path: 'home',
    loadComponent: () => import('./features/home/home').then(m => m.HomeComponent)
  },
  {
    path: 'mapa',
    loadComponent: () => import('./features/mapa/mapa').then(m => m.MapaComponent)
  },
  {
    path: 'triagem',
    canActivate: [authGuard],
    loadComponent: () => import('./features/triagem/triagem').then(m => m.TriagemComponent)
  },
  {
    path: 'dashboard',
    canActivate: [authGuard, gestorGuard],
    loadComponent: () => import('./features/dashboard/dashboard').then(m => m.DashboardComponent)
  },
  {
    path: 'logs',
    canActivate: [authGuard, gestorGuard],
    loadComponent: () => import('./features/logs/logs').then(m => m.LogsComponent)
  },
  { path: '**', redirectTo: 'login' }
];
