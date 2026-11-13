import { Routes } from '@angular/router';

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
    path: 'solicitacao/nova',
    loadComponent: () => import('./features/solicitacao/nova/nova-solicitacao').then(m => m.NovaSolicitacaoComponent)
  },
  {
    path: 'solicitacao/nova/localizacao',
    loadComponent: () => import('./features/solicitacao/localizacao/localizacao').then(m => m.LocalizacaoComponent)
  },
  { path: '**', redirectTo: 'login' }
];
