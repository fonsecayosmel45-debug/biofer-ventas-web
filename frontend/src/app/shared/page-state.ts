import { ChangeDetectorRef, Directive, inject } from '@angular/core';
import { ApiService, errorMessage, personName, today } from '../core/api.service';

@Directive()
export abstract class PageState {
  protected readonly api = inject(ApiService);
  protected readonly change = inject(ChangeDetectorRef);
  busy = false;
  error = '';
  personName = personName;
  today = today;
  async task(action: () => Promise<void>): Promise<void> {
    if (this.busy) return;
    this.busy = true; this.error = ''; this.change.markForCheck();
    try { await action(); }
    catch (error) { this.error = errorMessage(error); }
    finally { this.busy = false; this.change.markForCheck(); }
  }
}
