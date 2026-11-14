import { Component, input } from '@angular/core';
@Component({
  selector: 'app-icon',
  template: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.65" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path [attr.d]="paths[name()] || paths['box']" /></svg>`,
  styles: [':host{display:inline-flex;width:20px;height:20px;flex-shrink:0}svg{width:100%;height:100%}']
})
export class Icon {
  name = input('box');
  readonly paths: Record<string, string> = {
    filter: 'M3 4h18l-7 8v7l-4 2v-9L3 4Z',
    refresh: 'M20 7v5h-5M4 17v-5h5M5.5 7a7 7 0 0 1 12-2L20 8M4 16l2.5 3a7 7 0 0 0 12-2',
    download: 'M12 3v12m-5-5 5 5 5-5M4 16v5h16v-5',
    edit: 'm16 3 5 5M3 21l5-1L21 7a2 2 0 0 0-4-4L4 16l-1 5Z',
    sort: 'm8 4-4 4 4 4M4 4v16m12 0 4-4-4-4M20 4v16',
    warning: 'm12 3 10 18H2L12 3ZM12 9v5M12 17h.01',
    history: 'M3 3v6h6M3 9a9 9 0 1 1 0 6M12 7v5l3 2',
    eye: 'M2 12s3-7 10-7 10 7 10 7-3 7-10 7S2 12 2 12ZM15 12a3 3 0 1 1-6 0 3 3 0 0 1 6 0Z',
    eyeOff: 'm3 3 18 18M10.5 5.1 12 5c7 0 10 7 10 7a18 18 0 0 1-3 4M6 6a19 19 0 0 0-4 6s3 7 10 7c2 0 4-.6 5-1.5M9.9 9.9a3 3 0 0 0 4.2 4.2',
    leaf: 'M20 4C9 2 3 6 5 14c6 8 16 2 15-10ZM5 20 16 9M9 16v-5M13 12h5',
    grid: 'M3 3h7v7H3zM14 3h7v7h-7zM3 14h7v7H3zM14 14h7v7h-7z',
    box: 'm12 3 9 5v9l-9 5-9-5V8l9-5ZM3 8l9 5 9-5M12 13v9M7 5l9 5',
    users: 'M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2M17 4a4 4 0 0 1 0 8M22 21v-2a4 4 0 0 0-3-4M13 7a4 4 0 1 1-8 0 4 4 0 0 1 8 0Z',
    cart: 'M3 3h2l3 13h11l3-10H6M9 21h.01M19 21h.01M9 11h9',
    truck: 'M1 4h13v13H1zM14 9h5l3 4v4h-8M8 18a2 2 0 1 1-4 0 2 2 0 0 1 4 0ZM21 18a2 2 0 1 1-4 0 2 2 0 0 1 4 0Z',
    file: 'M14 2H4v20h16V8l-6-6ZM14 2v6h6M8 12h8M8 16h8',
    wallet: 'M20 8V4H4a2 2 0 0 0 0 4h18v12H4a2 2 0 0 1-2-2V6M22 12h-6v4h6M18 14h.01',
    chart: 'M3 3v18h18M7 16v-4M12 16V7M17 16v-7',
    exit: 'M9 3H3v18h6M14 7l5 5-5 5M7 12h12',
    plus: 'M12 5v14M5 12h14',
    arrow: 'M5 12h14M13 6l6 6-6 6',
    search: 'M21 21l-5-5M18 10a8 8 0 1 1-16 0 8 8 0 0 1 16 0Z',
    close: 'm6 6 12 12M6 18 18 6',
    check: 'm5 12 4 4L19 6',
    return: 'M3 10h11a6 6 0 0 1 0 12M3 10l6-6M3 10l6 6',
    menu: 'M3 6h18M3 12h18M3 18h18',
    bell: 'M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9M10 21h4'
  };
}
