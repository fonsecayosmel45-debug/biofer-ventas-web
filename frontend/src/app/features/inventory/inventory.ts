import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { PageState } from '../../shared/page-state';
import { Editor, FormField } from '../../shared/editor';
import { Icon } from '../../shared/icon';
import { FilterPanel, FilterField } from '../../shared/filter-panel';
import { categoryIds, matchesSearch, inDateRange, stockMatches, exportTable } from '../../shared/catalog-tools';

interface InventoryFilters {
  query:string;product:number|'';category:number|'';origin:string;status:string;stock:string;unit:string;currency:string;
  dateField:string;from:string;to:string;minPrice:number|null;maxPrice:number|null;
}
const newFilters=():InventoryFilters=>({query:'',product:'',category:'',origin:'',status:'',stock:'',unit:'',currency:'',dateField:'fechaCreacion',from:'',to:'',minPrice:null,maxPrice:null});

@Component({ selector:'app-inventory', imports:[CommonModule,FormsModule,Editor,Icon,FilterPanel], templateUrl:'./inventory.html' })
export class Inventory extends PageState implements OnInit {
  private readonly route=inject(ActivatedRoute);
  rows:any[]=[]; products:any[]=[]; categories:any[]=[]; model:any=null; fields:FormField[]=[];
  filters:InventoryFilters=newFilters();applied:InventoryFilters=newFilters();page=1;pageSize=10;sort='codigo';direction=1;readOnly=false;
  selected=new Set<number>();
  columns=[{key:'capacidad',label:'Capacidad / unidad',visible:true},{key:'precio',label:'Precio unitario sin IGV',visible:true},{key:'fisico',label:'Stock físico',visible:true},{key:'reservado',label:'Reservado',visible:true},{key:'disponible',label:'Disponible',visible:true},{key:'fechas',label:'Última actualización',visible:false}];
  editing:any=null; mode=''; history:any=null; historyRows:any[]=[];
  historySearch='';historyType='';historyFrom='';historyTo='';
  get historyFiltered():any[]{return this.historyRows.filter(h=>matchesSearch(`${h.motivo||''} ${h.referencia||''} ${h.tipo||''}`,this.historySearch)&&(!this.historyType||h.tipo===this.historyType)&&inDateRange(h.fecha,this.historyFrom,this.historyTo));}
  ngOnInit():void { void this.task(async()=>{
    await this.refresh();
    const params=this.route.snapshot.queryParamMap;
    const product=Number(params.get('producto'));
    if(this.products.some(p=>p.id===product)){this.filters.product=product;this.applyFilters();}
    const article=this.rows.find(p=>p.id===Number(params.get('editar'))&&(!product||p.productoId===product));
    if(article)this.open(article);
    else if(params.has('nuevo'))this.open();
  }); }
  private async refresh():Promise<void> { [this.rows,this.products,this.categories]=await Promise.all([this.api.all('presentaciones'),this.api.all('productos'),this.api.all('categorias')]); }
  load():Promise<void> { return this.task(()=>this.refresh()); }
  get filterFields():FilterField[]{return[
    {key:'query',label:'Buscar artículo',type:'search',wide:true,placeholder:'Código SKU, nombre comercial, producto o descripción…'},
    {key:'product',label:'Producto',type:'select',options:[{value:'',label:'Todos los productos'},...this.products.map(p=>({value:p.id,label:p.nombreComercial}))]},
    {key:'category',label:'Categoría',type:'select',options:[{value:'',label:'Todas las categorías'},...this.categories.map(c=>({value:c.id,label:c.padreId?`${this.categories.find(p=>p.id===c.padreId)?.nombre||''} / ${c.nombre}`:c.nombre}))]},
    {key:'stock',label:'Nivel de existencias',type:'select',options:[{value:'',label:'Todos los niveles'},{value:'bajo',label:'Bajo el mínimo disponible'},{value:'sin-stock',label:'Sin unidades disponibles'},{value:'estable',label:'Dentro del rango'},{value:'alto',label:'Sobre el máximo físico'},{value:'reservado',label:'Con reservas'},{value:'con-stock',label:'Con disponibilidad'}]},
    {key:'status',label:'Estado',type:'select',options:[{value:'',label:'Todos los estados'},{value:'activo',label:'Habilitados para operar'},{value:'inactivo',label:'Inactivos'}]},
    {key:'currency',label:'Moneda',type:'select',options:[{value:'',label:'Todas las monedas'},...[...new Set<string>(this.rows.map(p=>p.moneda))].sort().map(v=>({value:v,label:v}))]},
    {key:'unit',label:'Unidad de medida',type:'select',options:[{value:'',label:'Todas las unidades'},...[...new Set<string>(this.rows.map(p=>p.unidad))].sort().map(v=>({value:v,label:v}))]},
    {key:'origin',label:'Procedencia',type:'select',advanced:true,options:[{value:'',label:'Todas'},{value:'NACIONAL',label:'Nacional'},{value:'ESPANOL',label:'Español'}]},
    {key:'minPrice',label:'Precio sin IGV desde',type:'number',advanced:true,min:0,hint:'Selecciona una moneda para comparar precios.'},{key:'maxPrice',label:'Precio sin IGV hasta',type:'number',advanced:true,min:0},
    {key:'dateField',label:'Fecha a consultar',type:'select',advanced:true,options:[{value:'fechaCreacion',label:'Creación de la presentación'},{value:'fechaActualizacion',label:'Última actualización'}]},
    {key:'from',label:'Desde',type:'date',advanced:true},{key:'to',label:'Hasta',type:'date',advanced:true}
  ];}
  get filterError():string{
    const f=this.filters;
    if(f.from&&f.to&&f.from>f.to)return 'La fecha inicial no puede ser posterior a la fecha final.';
    if((f.minPrice!==null||f.maxPrice!==null)&&!f.currency)return 'Selecciona una moneda antes de filtrar por precio.';
    if(f.minPrice!==null&&f.maxPrice!==null&&f.minPrice>f.maxPrice)return 'El precio mínimo no puede superar al máximo.';
    return '';
  }
  get filtered():any[]{
    const f=this.applied;const family=f.category?categoryIds(this.categories,Number(f.category)):null;
    return this.rows.filter(p=>{
      const product=this.products.find(item=>item.id===p.productoId);const active=p.activa&&product?.activo!==false;
      return matchesSearch(`${p.nombreComercial} ${p.codigo} ${product?.nombreProducto||''} ${product?.descripcion||''}`,f.query)
        &&(!f.product||p.productoId===Number(f.product))&&(!family||product?.categorias.some((c:any)=>family.has(c.id)))
        &&(!f.origin||product?.procedencia===f.origin)&&(!f.status||active===(f.status==='activo'))&&(!f.currency||p.moneda===f.currency)&&(!f.unit||p.unidad===f.unit)
        &&stockMatches([p],f.stock)&&inDateRange(p[f.dateField],f.from,f.to)&&(f.minPrice===null||Number(p.precioSinIgv)>=f.minPrice)&&(f.maxPrice===null||Number(p.precioSinIgv)<=f.maxPrice);
    }).sort((a,b)=>{
      let n=0;if(this.sort==='stock')n=a.stockDisponible-b.stockDisponible;
      else if(this.sort==='precio')n=String(a.moneda).localeCompare(b.moneda)||Number(a.precioSinIgv)-Number(b.precioSinIgv);
      else if(this.sort==='fecha')n=String(a.fechaActualizacion||'').localeCompare(String(b.fechaActualizacion||''));
      else n=String(this.sort==='nombre'?a.nombreComercial:a.codigo).localeCompare(String(this.sort==='nombre'?b.nombreComercial:b.codigo),'es',{numeric:true,sensitivity:'base'});
      return n*this.direction;
    });
  }
  get lowCount():number{return this.rows.filter(p=>p.activa&&stockMatches([p],'bajo')).length;}
  get zeroCount():number{return this.rows.filter(p=>p.activa&&p.stockDisponible===0).length;}
  get activeCount():number{return this.rows.filter(p=>p.activa).length;}
  get pages():number{return Math.max(1,Math.ceil(this.filtered.length/this.pageSize));}
  get currentPage():number{return Math.min(this.page,this.pages);}
  get visibleRows():any[]{return this.filtered.slice((this.currentPage-1)*this.pageSize,this.currentPage*this.pageSize);}
  get rangeStart():number{return this.filtered.length?(this.currentPage-1)*this.pageSize+1:0;}
  get rangeEnd():number{return Math.min(this.currentPage*this.pageSize,this.filtered.length);}
  get chips():{key:keyof InventoryFilters;label:string}[]{
    const f=this.applied;const chips:{key:keyof InventoryFilters;label:string}[]=[];
    for(const field of this.filterFields){const key=field.key as keyof InventoryFilters;const value=f[key];if(key==='dateField'||value===''||value===null)continue;chips.push({key,label:`${field.label}: ${field.options?.find(o=>o.value===value)?.label??value}`});}return chips;
  }
  applyFilters():void{if(this.filterError)return;this.applied={...this.filters,query:this.filters.query.trim()};this.page=1;this.selected.clear();}
  clearFilters():void{this.filters=newFilters();this.applyFilters();}
  removeFilter(key:keyof InventoryFilters):void{this.filters={...this.applied,[key]:newFilters()[key]};if(key==='currency'){this.filters.minPrice=null;this.filters.maxPrice=null;}this.applyFilters();}
  quickFilter(type:string):void{
    this.filters={...this.applied};
    if(type==='activos'){this.filters.status=this.applied.status==='activo'&&!this.applied.stock?'':'activo';this.filters.stock='';}
    else{this.filters.status='activo';this.filters.stock=this.applied.stock===type?'':type;}
    this.applyFilters();
  }
  orderBy(key:string):void{this.direction=this.sort===key?-this.direction:1;this.sort=key;this.page=1;}
  visibleColumn(key:string):boolean{return this.columns.some(c=>c.key===key&&c.visible);}
  get columnCount():number{return 4+this.columns.filter(c=>c.visible).length;}
  toggle(id:number):void{this.selected.has(id)?this.selected.delete(id):this.selected.add(id);}
  get pageSelected():boolean{return !!this.visibleRows.length&&this.visibleRows.every(r=>this.selected.has(r.id));}
  togglePage():void{const remove=this.pageSelected;for(const r of this.visibleRows){if(remove)this.selected.delete(r.id);else this.selected.add(r.id);}}
  exportCsv():void{
    const rows=this.filtered.filter(r=>!this.selected.size||this.selected.has(r.id));
    exportTable(`biofer-inventario-${this.today()}.csv`,['Código','Producto','Capacidad','Unidad','Precio sin IGV','Moneda','Físico','Reservado','Disponible','Mínimo','Alto','Estado'],rows.map(p=>[p.codigo,p.nombreComercial,p.capacidad,p.unidad,p.precioSinIgv,p.moneda,p.stockActual,p.stockReservado,p.stockDisponible,p.stockMinimo,p.stockAlto,p.activa?'Activo':'Inactivo']));this.api.success(`${rows.length} artículos exportados`);
  }
  fillPercent(article:any):number{return Math.max(0,Math.min(100,100*article.stockDisponible/Math.max(1,article.stockAlto)));}
  get available():number { return this.rows.reduce((n,p)=>n+p.stockDisponible,0); }
  get reserved():number { return this.rows.reduce((n,p)=>n+p.stockReservado,0); }
  stockLabel(article:any):string {
    if (article.stockDisponible === 0) return 'Sin disponible';
    return article.stockDisponible < article.stockMinimo ? 'Stock bajo' : 'Disponible';
  }
  open(row?:any,readOnly=false):void {
    this.editing=row||null; this.mode='presentacion'; this.error='';this.readOnly=readOnly;
    this.model=row?{...row,otraUnidad:row.otraUnidad||null,motivoCambioPrecio:''}:{productoId:this.applied.product||null,codigo:'',capacidad:1,unidad:'LT',otraUnidad:null,precioSinIgv:null,moneda:'PEN',stockMinimo:5,stockAlto:100,activa:true,motivoCambioPrecio:''};
    this.fields=[{key:'codigo',label:'Código de artículo',required:true,max:40},{key:'precioSinIgv',label:'Precio unitario sin IGV',type:'number',min:0.0001,step:'0.0001',required:true,hint:'Precio de una unidad de esta presentación. En el pedido: precio unitario × cantidad.'},{key:'moneda',label:'Moneda (código ISO)',required:true,max:3},{key:'stockMinimo',label:'Stock mínimo',type:'number',min:0,step:'1',required:true},{key:'stockAlto',label:'Stock alto',type:'number',min:0,step:'1',required:true},{key:'activa',label:'Presentación activa',type:'checkbox'}];
    if(!row)this.fields.unshift({key:'productoId',label:'Producto',type:'select',required:true,options:this.products.filter(p=>p.activo).map(p=>({value:p.id,label:p.nombreComercial}))},{key:'capacidad',label:'Capacidad',type:'number',min:0.001,step:'0.001',required:true},{key:'unidad',label:'Unidad',type:'select',required:true,options:['ML','LT','GR','KG','OTRA'].map(v=>({value:v,label:v}))},{key:'otraUnidad',label:'Otra unidad (solo si elegiste OTRA)',max:50});
    else this.fields.push({key:'motivoCambioPrecio',label:'Motivo del cambio de precio o moneda',type:'textarea',hint:'Obligatorio si cambias precio o moneda.',max:500});
  }
  movement(row:any):void {
    this.editing=row;this.mode='movimiento';this.error='';this.readOnly=false;this.model={tipo:'ENTRADA',cantidad:null,motivo:'',referencia:''};
    this.fields=[{key:'tipo',label:'Movimiento',type:'select',required:true,options:[{value:'ENTRADA',label:'Entrada de existencias'},{value:'AJUSTE_POSITIVO',label:'Ajuste positivo'},{value:'AJUSTE_NEGATIVO',label:'Ajuste negativo'}]},{key:'cantidad',label:'Cantidad de presentaciones',type:'number',min:1,step:'1',required:true},{key:'motivo',label:'Motivo',type:'textarea',required:true,max:500},{key:'referencia',label:'Referencia',max:100}];
  }
  save():Promise<void>{return this.task(async()=>{
    if(this.mode==='movimiento')await this.api.write('POST',`presentaciones/${this.editing.id}/movimientos`,this.model,false);
    else { const m=this.model;await this.api.write(this.editing?'PUT':'POST',`presentaciones${this.editing?'/'+this.editing.id:''}`,{productoId:m.productoId,codigo:m.codigo,capacidad:m.capacidad,unidad:m.unidad,otraUnidad:m.otraUnidad||null,precioSinIgv:m.precioSinIgv,moneda:m.moneda.toUpperCase(),stockMinimo:m.stockMinimo,stockAlto:m.stockAlto,activa:m.activa,motivoCambioPrecio:m.motivoCambioPrecio||null},false); }
    this.model=null;this.api.success('Inventario actualizado');await this.refresh();
  });}
  showHistory(row:any,type:string):Promise<void>{return this.task(async()=>{
    this.history={row,type};this.historyRows=[];this.historySearch='';this.historyType='';this.historyFrom='';this.historyTo='';this.historyRows=await this.api.all(`presentaciones/${row.id}/${type}`);
  });}
}
