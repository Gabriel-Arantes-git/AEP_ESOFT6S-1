import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-bottom-nav',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './bottom-nav.html',
  styleUrl: './bottom-nav.scss',
})
export class BottomNavComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  get loggedIn(): boolean {
    return this.auth.isAuthenticated();
  }

  get isGestor(): boolean {
    return this.auth.isGestor();
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
