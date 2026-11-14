import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { errorMessage } from '../../core/api.service';
import { Icon } from '../../shared/icon';

@Component({ selector: 'app-login', imports: [FormsModule, Icon], templateUrl: './login.html', styleUrl: './login.scss' })
export class Login {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  usuario = localStorage.getItem('biofer.remembered-user') || 'admin'; clave = ''; visible = false;
  remember = !!localStorage.getItem('biofer.remembered-user');
  readonly error = signal(''); readonly busy = signal(false);
  constructor() { if (this.auth.valid()) void this.router.navigate(['/inicio']); }
  rememberUser():void {
    if(this.remember)localStorage.setItem('biofer.remembered-user',this.usuario.trim());
    else localStorage.removeItem('biofer.remembered-user');
  }
  async enter(): Promise<void> {
    if (this.busy()) return;
    this.busy.set(true); this.error.set('');
    try { await this.auth.login(this.usuario.trim(), this.clave); this.rememberUser(); this.clave = ''; await this.router.navigate(['/inicio']); }
    catch (error) { this.error.set(errorMessage(error)); }
    finally { this.busy.set(false); }
  }
}
