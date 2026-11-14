import { Component, OnDestroy, inject, signal } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { AuthService } from '../core/auth.service';
import { ApiService } from '../core/api.service';
import { Icon } from '../shared/icon';

interface NavGroup {
  key:string;
  label:string;
  icon:string;
  links:{path:string;label:string}[];
}

@Component({
  selector: 'app-shell', imports: [RouterLink, RouterLinkActive, RouterOutlet, Icon], templateUrl: './shell.html'
})
export class Shell implements OnDestroy {
  readonly auth = inject(AuthService);
  readonly api = inject(ApiService);
  private readonly router=inject(Router);
  readonly currentPath=signal(this.router.url.split('?')[0]);
  private readonly navigation=this.router.events.pipe(filter((event):event is NavigationEnd=>event instanceof NavigationEnd)).subscribe(event=>this.currentPath.set(event.urlAfterRedirects.split('?')[0]));
  open = window.innerWidth > 960;
  expanded:Record<string,boolean>={catalogo:true,comercial:true,administracion:true,seguimiento:false};
  readonly groups:NavGroup[]=[
    {key:'catalogo',label:'Productos e inventario',icon:'box',links:[{path:'/productos',label:'Productos y categorías'},{path:'/inventario',label:'Presentaciones y stock'}]},
    {key:'comercial',label:'Comercial',icon:'cart',links:[{path:'/pedidos',label:'Pedidos'},{path:'/documentos',label:'Despachos y documentos'},{path:'/cobranzas',label:'Crédito y cobranzas'},{path:'/devoluciones',label:'Devoluciones'}]},
    {key:'administracion',label:'Administración',icon:'users',links:[{path:'/clientes',label:'Clientes'},{path:'/vendedores',label:'Vendedores'}]},
    {key:'seguimiento',label:'Seguimiento',icon:'chart',links:[{path:'/reportes',label:'Reportes y auditoría'}]}
  ];
  get title():string{return this.groups.flatMap(g=>g.links).find(link=>link.path===this.currentPath())?.label||'Inicio';}
  groupActive(group:NavGroup):boolean{return group.links.some(link=>link.path===this.currentPath());}
  navigate():void{if(window.innerWidth<=960)this.open=false;}
  ngOnDestroy():void{this.navigation.unsubscribe();}
}
