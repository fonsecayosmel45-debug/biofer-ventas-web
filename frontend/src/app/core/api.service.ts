import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

export interface Page<T> { content: T[]; totalElements: number; totalPages: number; number: number; }
export function personName(p: any): string {
  const d = p?.datos || p;
  return d?.razonSocial || [d?.nombres, d?.apellidos].filter(Boolean).join(' ') || 'Sin nombre';
}
export function today(): string {
  const parts = new Intl.DateTimeFormat('en-CA', { timeZone: 'America/Lima', year: 'numeric', month: '2-digit', day: '2-digit' }).formatToParts(new Date());
  const part = (type: string) => parts.find(p => p.type === type)?.value;
  return `${part('year')}-${part('month')}-${part('day')}`;
}
export function errorMessage(error: unknown): string {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0) return 'No se pudo conectar con el servidor. Comprueba que el backend esté encendido.';
    if (error.status === 401) return error.error?.mensaje || 'La sesión venció. Vuelve a iniciar sesión.';
    const fields = error.error?.campos;
    const detail = fields ? Object.entries(fields).map(([key, value]) => `${key}: ${value}`).join(' · ') : '';
    return [error.error?.mensaje || error.error?.detail || `La operación no se completó (${error.status}).`, detail].filter(Boolean).join(' ');
  }
  return error instanceof Error ? error.message : 'No se pudo completar la operación.';
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly keys = new Map<string, string>();
  readonly notice = signal('');
  private timer?: ReturnType<typeof setTimeout>;
  get<T = any>(path: string): Promise<T> { return firstValueFrom(this.http.get<T>(`/api/${path}`)); }
  page<T = any>(resource: string, page = 0, size = 12): Promise<Page<T>> {
    return this.get<Page<T>>(`${resource}?page=${page}&size=${size}&sort=id,desc`);
  }
  async all<T = any>(resource: string): Promise<T[]> {
    const rows: T[] = [];
    let page = 0;
    while (true) {
      const result = await this.page<T>(resource, page++, 200);
      rows.push(...result.content);
      if (page >= result.totalPages) return rows;
    }
  }
  async write<T = any>(method: 'POST' | 'PUT' | 'DELETE', path: string, body: unknown = {}, idempotent = true): Promise<T> {
    const fingerprint = `${method}:${path}:${JSON.stringify(body)}`;
    let headers = new HttpHeaders();
    if (idempotent) {
      const key = this.keys.get(fingerprint) || crypto.randomUUID();
      this.keys.set(fingerprint, key);
      headers = headers.set('Idempotency-Key', key);
    }
    try {
      const result = await firstValueFrom(this.http.request<T>(method, `/api/${path}`, { body, headers }));
      this.keys.delete(fingerprint);
      return result;
    } catch (error) {
      if (error instanceof HttpErrorResponse && error.status >= 400 && error.status < 500) this.keys.delete(fingerprint);
      throw error;
    }
  }
  async file(path: string, name: string): Promise<void> {
    const blob = await firstValueFrom(this.http.get(`/api/${path}`, { responseType: 'blob' }));
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a'); link.href = url; link.download = name; link.click();
    setTimeout(() => URL.revokeObjectURL(url), 10000);
  }
  async upload(file: File): Promise<string> {
    const form = new FormData(); form.append('archivo', file);
    const result = await firstValueFrom(this.http.post<{ archivo: string }>('/api/archivos', form));
    return result.archivo;
  }
  success(message: string): void {
    clearTimeout(this.timer); this.notice.set(message);
    this.timer = setTimeout(() => this.notice.set(''), 5000);
  }
}
