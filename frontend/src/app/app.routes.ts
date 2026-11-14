import { Routes } from '@angular/router';
import { authGuard } from './core/auth.service';
import { Shell } from './layout/shell';

export const routes: Routes = [
  {path:'login',loadComponent:()=>import('./features/login/login').then(m=>m.Login)},
  {path:'',component:Shell,canActivate:[authGuard],canActivateChild:[authGuard],children:[
    {path:'',pathMatch:'full',redirectTo:'inicio'},
    {path:'inicio',loadComponent:()=>import('./features/dashboard/dashboard').then(m=>m.Dashboard)},
    {path:'productos',loadComponent:()=>import('./features/catalog/catalog').then(m=>m.Catalog)},
    {path:'inventario',loadComponent:()=>import('./features/inventory/inventory').then(m=>m.Inventory)},
    {path:'clientes',data:{resource:'clientes'},loadComponent:()=>import('./features/people/people').then(m=>m.People)},
    {path:'vendedores',data:{resource:'vendedores'},loadComponent:()=>import('./features/people/people').then(m=>m.People)},
    {path:'pedidos',loadComponent:()=>import('./features/orders/orders').then(m=>m.Orders)},
    {path:'documentos',loadComponent:()=>import('./features/documents/documents').then(m=>m.Documents)},
    {path:'cobranzas',loadComponent:()=>import('./features/finance/finance').then(m=>m.Finance)},
    {path:'devoluciones',loadComponent:()=>import('./features/returns/returns').then(m=>m.Returns)},
    {path:'reportes',loadComponent:()=>import('./features/reports/reports').then(m=>m.Reports)}
  ]},
  {path:'**',redirectTo:'inicio'}
];
