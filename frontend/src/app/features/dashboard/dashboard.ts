import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { PageState } from '../../shared/page-state';
import { Icon } from '../../shared/icon';

@Component({ selector: 'app-dashboard', imports: [CommonModule, RouterLink, Icon], templateUrl: './dashboard.html' })
export class Dashboard extends PageState implements OnInit {
  inventory: any[] = []; orders: any[] = []; alerts: any[] = []; customers = 0; articles = 0; count = 0;
  debts: { currency: string; amount: number }[] = [];
  ngOnInit(): void { void this.load(); }
  load(): Promise<void> { return this.task(async () => {
    const [stock, orders, alerts, clients, articles, invoices] = await Promise.all([
      this.api.get<any[]>('reportes/stock'), this.api.page('pedidos', 0, 6), this.api.get<any[]>('reportes/vencimientos?diasAviso=7'),
      this.api.page('clientes', 0, 1), this.api.page('presentaciones', 0, 1), this.api.all('facturas')
    ]);
    this.inventory = stock; this.orders = orders.content; this.count = orders.totalElements;
    this.alerts = alerts; this.customers = clients.totalElements; this.articles = articles.totalElements;
    const sums = new Map<string, number>();
    for (const invoice of invoices) sums.set(invoice.moneda, (sums.get(invoice.moneda) || 0) + Number(invoice.saldo));
    this.debts = [...sums].map(([currency, amount]) => ({ currency, amount }));
  }); }
  get lowStock(): number { return this.inventory.filter(p => p.disponible < p.stockMinimo || p.disponible === 0).length; }
}
