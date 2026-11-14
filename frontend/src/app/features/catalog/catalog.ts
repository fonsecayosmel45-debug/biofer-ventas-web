import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { PageState } from '../../shared/page-state';
import { Editor, FormField } from '../../shared/editor';
import { Icon } from '../../shared/icon';
import { FilterPanel, FilterField } from '../../shared/filter-panel';
import { CatalogFilters, newCatalogFilters, categoryIds, matchesSearch, inDateRange, stockMatches, exportTable } from '../../shared/catalog-tools';

@Component({ selector: 'app-catalog', imports: [CommonModule, FormsModule, Editor, Icon, FilterPanel, RouterLink], templateUrl: './catalog.html' })
export class Catalog extends PageState implements OnInit {
  tab: 'productos' | 'categorias' = 'productos'; products: any[] = []; categories: any[] = []; presentations:any[]=[];
  filters:CatalogFilters=newCatalogFilters();applied:CatalogFilters=newCatalogFilters();
  page=1;pageSize=10;sort='nombre';direction=1;readOnly=false;selected=new Set<number>();
  columns=[{key:'origen',label:'Procedencia',visible:true},{key:'categorias',label:'Categorías',visible:true},{key:'stock',label:'Existencias',visible:true},{key:'fechas',label:'Última actualización',visible:false}];
  private articles=new Map<number,any[]>();
  model: any = null; fields: FormField[] = []; editing: number | null = null;
  ngOnInit(): void { void this.load(); }
  private async refresh(): Promise<void> {
    [this.products,this.categories,this.presentations]=await Promise.all([this.api.all('productos'),this.api.all('categorias'),this.api.all('presentaciones')]);
    this.articles.clear();for(const p of this.presentations)this.articles.set(p.productoId,[...(this.articles.get(p.productoId)||[]),p]);
  }
  load(): Promise<void> { return this.task(() => this.refresh()); }
  get filterFields():FilterField[]{
    const status={key:'status',label:'Estado',type:'select' as const,options:[{value:'',label:'Todos los estados'},{value:'activo',label:'Activos'},{value:'inactivo',label:'Inactivos'}]};
    const search={key:'query',label:this.tab==='productos'?'Buscar producto':'Buscar categoría',type:'search' as const,wide:true,placeholder:this.tab==='productos'?'Nombre comercial, producto, SKU o descripción…':'Nombre de categoría o categoría superior…'};
    if(this.tab==='categorias')return[search,status,{key:'level',label:'Nivel de categoría',type:'select',options:[{value:'',label:'Todos los niveles'},{value:'principal',label:'Solo principales'},{value:'subcategoria',label:'Solo subcategorías'}]},{key:'parent',label:'Categoría superior',type:'select',options:[{value:'',label:'Todas las familias'},...this.categoryOptions]}];
    return[search,{key:'category',label:'Categoría',type:'select',options:[{value:'',label:'Todas las categorías'},...this.categoryOptions],hint:'Incluye las subcategorías de la familia elegida.'},{key:'origin',label:'Procedencia',type:'select',options:[{value:'',label:'Todas'},{value:'NACIONAL',label:'Nacional'},{value:'ESPANOL',label:'Español'}]},status,
      {key:'stock',label:'Existencias de sus presentaciones',type:'select',options:[{value:'',label:'Todos los niveles'},{value:'con-stock',label:'Con unidades disponibles'},{value:'bajo',label:'Bajo el mínimo disponible'},{value:'sin-stock',label:'Sin unidades disponibles'},{value:'reservado',label:'Con unidades reservadas'},{value:'sin-presentacion',label:'Sin presentaciones'}]},
      {key:'dateField',label:'Fecha a consultar',type:'select',advanced:true,options:[{value:'fechaCreacion',label:'Creación del producto'},{value:'fechaActualizacion',label:'Última actualización'}]},{key:'from',label:'Desde',type:'date',advanced:true},{key:'to',label:'Hasta',type:'date',advanced:true}];
  }
  get categoryOptions():{value:number;label:string}[]{return this.categories.map(c=>({value:c.id,label:c.padreId?`${this.categoryName(c.padreId)} / ${c.nombre}`:c.nombre})).sort((a,b)=>a.label.localeCompare(b.label,'es'));}
  get filterError():string{return this.filters.from&&this.filters.to&&this.filters.from>this.filters.to?'La fecha inicial no puede ser posterior a la fecha final.':'';}
  get rows():any[]{
    const f=this.applied;const family=f.category?categoryIds(this.categories,Number(f.category)):null;
    const result=(this.tab==='productos'?this.products:this.categories).filter(p=>{
      const active=this.tab==='productos'?p.activo:p.activa;
      if(f.status&&active!==(f.status==='activo'))return false;
      if(this.tab==='categorias')return matchesSearch(`${p.nombre} ${p.padreId?this.categoryName(p.padreId):''}`,f.query)&&(!f.level||(f.level==='principal'?!p.padreId:!!p.padreId))&&(!f.parent||p.padreId===Number(f.parent));
      const items=this.related(p.id);
      return matchesSearch(`${p.nombreComercial} ${p.nombreProducto} ${p.descripcion||''} ${(p.categorias||[]).map((c:any)=>c.nombre).join(' ')} ${items.map(a=>a.codigo).join(' ')}`,f.query)
        &&(!family||p.categorias.some((c:any)=>family.has(c.id)))&&(!f.origin||p.procedencia===f.origin)&&stockMatches(items,f.stock)&&inDateRange(p[f.dateField],f.from,f.to);
    });
    return result.sort((a,b)=>{let value=0;if(this.sort==='id')value=a.id-b.id;else if(this.sort==='stock')value=this.available(a.id)-this.available(b.id);else if(this.sort==='fecha')value=String(a.fechaActualizacion||'').localeCompare(String(b.fechaActualizacion||''));else value=String(a.nombreComercial||a.nombre).localeCompare(String(b.nombreComercial||b.nombre),'es',{numeric:true,sensitivity:'base'});return value*this.direction;});
  }
  related(id:number):any[]{return this.articles.get(id)||[];}
  physical(id:number):number{return this.related(id).reduce((n,p)=>n+Number(p.stockActual),0);}
  available(id:number):number{return this.related(id).reduce((n,p)=>n+Number(p.stockDisponible),0);}
  reserved(id:number):number{return this.related(id).reduce((n,p)=>n+Number(p.stockReservado),0);}
  get lowCount():number{return this.products.filter(p=>p.activo&&stockMatches(this.related(p.id),'bajo')).length;}
  get zeroCount():number{return this.products.filter(p=>p.activo&&stockMatches(this.related(p.id),'sin-stock')).length;}
  get activeCount():number{return(this.tab==='productos'?this.products:this.categories).filter(p=>p.activo||p.activa).length;}
  get mainCount():number{return this.categories.filter(c=>!c.padreId).length;}
  get subCount():number{return this.categories.filter(c=>!!c.padreId).length;}
  get withoutPresentationCount():number{return this.products.filter(p=>p.activo&&!this.related(p.id).length).length;}
  childCount(id:number):number{return this.categories.filter(c=>c.padreId===id).length;}
  get pages():number{return Math.max(1,Math.ceil(this.rows.length/this.pageSize));}
  get currentPage():number{return Math.min(this.page,this.pages);}
  get visibleRows():any[]{return this.rows.slice((this.currentPage-1)*this.pageSize,this.currentPage*this.pageSize);}
  get rangeStart():number{return this.rows.length?(this.currentPage-1)*this.pageSize+1:0;}
  get rangeEnd():number{return Math.min(this.currentPage*this.pageSize,this.rows.length);}
  get chips():{key:keyof CatalogFilters;label:string}[]{
    const f=this.applied;const result:{key:keyof CatalogFilters;label:string}[]=[];
    for(const field of this.filterFields){const key=field.key as keyof CatalogFilters;if(key==='dateField'||!f[key])continue;const option=field.options?.find(o=>o.value===f[key]);result.push({key,label:`${field.label}: ${option?.label||f[key]}`});}
    return result;
  }
  applyFilters():void{if(this.filterError)return;this.applied={...this.filters,query:this.filters.query.trim()};this.page=1;this.selected.clear();}
  clearFilters():void{this.filters=newCatalogFilters();this.applyFilters();}
  removeFilter(key:keyof CatalogFilters):void{this.filters={...this.applied,[key]:newCatalogFilters()[key]};this.applyFilters();}
  switchTab(tab:'productos'|'categorias'):void{this.tab=tab;this.sort='nombre';this.direction=1;this.clearFilters();}
  quickFilter(type:string):void{
    if(!type){this.clearFilters();return;}
    this.filters={...this.applied};
    if(type==='activos'){this.filters.status=this.applied.status==='activo'&&!this.applied.stock?'':'activo';this.filters.stock='';}
    else if(type==='principal'||type==='subcategoria')this.filters.level=this.applied.level===type?'':type;
    else{this.filters.status='activo';this.filters.stock=this.applied.stock===type?'':type;}
    this.applyFilters();
  }
  orderBy(key:string):void{this.direction=this.sort===key?-this.direction:1;this.sort=key;this.page=1;}
  visibleColumn(key:string):boolean{return this.columns.some(c=>c.key===key&&c.visible);}
  get columnCount():number{return this.tab==='categorias'?6:4+this.columns.filter(c=>c.visible).length;}
  toggle(id:number):void{this.selected.has(id)?this.selected.delete(id):this.selected.add(id);}
  get pageSelected():boolean{return this.visibleRows.length>0&&this.visibleRows.every(r=>this.selected.has(r.id));}
  togglePage():void{const remove=this.pageSelected;for(const row of this.visibleRows){if(remove)this.selected.delete(row.id);else this.selected.add(row.id);}}
  exportCsv():void{
    const rows=this.rows.filter(r=>!this.selected.size||this.selected.has(r.id));
    const headers=this.tab==='productos'?['ID','Nombre comercial','Nombre del producto','Procedencia','Categorías','Presentaciones','Stock físico (unidades)','Disponible (unidades)','Reservado (unidades)','Estado']:['ID','Categoría','Categoría superior','Estado'];
    const data=rows.map(r=>this.tab==='productos'?[r.id,r.nombreComercial,r.nombreProducto,r.procedencia,r.categorias.map((c:any)=>c.nombre).join(' / '),this.related(r.id).length,this.physical(r.id),this.available(r.id),this.reserved(r.id),r.activo?'Activo':'Inactivo']:[r.id,r.nombre,this.categoryName(r.padreId),r.activa?'Activo':'Inactivo']);
    exportTable(`biofer-${this.tab}-${this.today()}.csv`,headers,data);this.api.success(`${rows.length} registros exportados`);
  }
  categoryName(id: number): string { return this.categories.find(c => c.id === id)?.nombre || 'Categoría principal'; }
  open(row?: any,readOnly=false): void {
    this.error = ''; this.editing = row?.id || null;this.readOnly=readOnly;
    if (this.tab === 'categorias') {
      this.model = { nombre: row?.nombre || '', padreId: row?.padreId || null, activa: row?.activa ?? true };
      this.fields = [ { key: 'nombre', label: 'Nombre de categoría', required: true, max: 100 }, { key: 'padreId', label: 'Categoría superior (opcional)', type: 'select', options: this.categories.filter(c => c.id !== row?.id && c.activa).map(c => ({ value: c.id, label: c.nombre })) }, { key: 'activa', label: 'Categoría activa', type: 'checkbox' } ];
    } else {
      this.model = { nombreComercial: row?.nombreComercial || '', nombreProducto: row?.nombreProducto || '', procedencia: row?.procedencia || 'NACIONAL', descripcion: row?.descripcion || '', activo: row?.activo ?? true, categoriasIds: row?.categorias?.map((c: any) => c.id) || [] };
      this.fields = [ { key:'nombreComercial',label:'Nombre comercial',required:true,max:150 }, {key:'nombreProducto',label:'Nombre del producto',required:true,max:150}, {key:'procedencia',label:'Procedencia',type:'select',required:true,options:[{value:'NACIONAL',label:'Nacional'},{value:'ESPANOL',label:'Español'}]}, {key:'categoriasIds',label:'Categorías',type:'multi',required:true,options:this.categories.filter(c=>c.activa).map(c=>({value:c.id,label:c.nombre})),hint:'Mantén Ctrl para seleccionar varias categorías.'}, {key:'descripcion',label:'Descripción',type:'textarea',max:2000}, {key:'activo',label:'Producto activo',type:'checkbox'} ];
    }
  }
  save(): Promise<void> { return this.task(async () => {
    await this.api.write(this.editing ? 'PUT' : 'POST', `${this.tab}${this.editing ? '/' + this.editing : ''}`, this.model, false);
    this.model = null; this.api.success('Registro guardado correctamente'); await this.refresh();
  }); }
}
