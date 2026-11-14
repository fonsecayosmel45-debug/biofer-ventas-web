import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpInterceptorFn } from '@angular/common/http';
import { CanActivateFn, Router } from '@angular/router';
import { firstValueFrom, catchError, throwError } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  readonly user = signal(sessionStorage.getItem('biofer.user') || 'Administración');
  token(): string | null { return sessionStorage.getItem('biofer.token'); }
  valid(): boolean {
    try {
      const token = this.token(); if (!token) return false;
      const payload = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
      return JSON.parse(atob(payload)).exp * 1000 > Date.now();
    } catch { return false; }
  }
  async login(usuario: string, clave: string): Promise<void> {
    const result = await firstValueFrom(this.http.post<{ token: string; usuario: string }>('/api/auth/login', { usuario, clave }));
    sessionStorage.setItem('biofer.token', result.token); sessionStorage.setItem('biofer.user', result.usuario);
    this.user.set(result.usuario);
  }
  logout(): void {
    sessionStorage.removeItem('biofer.token'); sessionStorage.removeItem('biofer.user');
    void this.router.navigate(['/login']);
  }
}
export const authGuard: CanActivateFn = () => inject(AuthService).valid() || inject(Router).createUrlTree(['/login']);
export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const login = request.url.endsWith('/auth/login');
  const api = request.url.startsWith('/api/');
  const token = auth.token();
  return next(api && token && !login ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : request).pipe(
    catchError(error => {
      if (api && !login && error.status === 401) auth.logout();
      return throwError(() => error);
    })
  );
};
