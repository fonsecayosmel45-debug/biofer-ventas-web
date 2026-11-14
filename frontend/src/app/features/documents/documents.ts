import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PageState } from '../../shared/page-state';
import { Editor, FormField } from '../../shared/editor';
import { Icon } from '../../shared/icon';

@Component({selector:'app-documents',imports:[CommonModule,FormsModule,Editor,Icon],templateUrl:'./documents.html'})
export class Documents extends PageState implements OnInit {
  tab='despachos';shipments:any[]=[];invoices:any[]=[];guides:any[]=[];selected:any=null;
  model:any=null;fields:FormField[]=[];mode='';target:any=null;invoiceDraft:any=null;invoiceLines:any[]=[];currencies:string[]=[];
  ngOnInit():void{void this.load();}
  private async refresh():Promise<void>{[this.shipments,this.invoices,this.guides]=await Promise.all([this.api.all('despachos'),this.api.all('facturas'),this.api.all('guias-remision')]);}
  load():Promise<void>{return this.task(()=>this.refresh());}
  get rows():any[]{return this.tab==='despachos'?this.shipments:this.tab==='facturas'?this.invoices:this.guides;}
  hasGuide(id:number):boolean{return this.guides.some(g=>g.despachoId===id);}
  view(row:any):Promise<void>{return this.task(async()=>{this.selected=await this.api.get(`${this.tab}/${row.id}`);});}
  invoice(row:any):Promise<void>{return this.task(async()=>{
    const shipment=await this.api.get(`despachos/${row.id}`);const pedido=await this.api.get(`pedidos/${row.pedidoId}`);
    this.invoiceLines=shipment.detalles.filter((d:any)=>d.cantidad>d.facturada).map((d:any)=>{const l=pedido.detalles.find((x:any)=>x.id===d.detallePedidoId);return{...d,descripcion:l.descripcion,moneda:l.moneda,pendiente:d.cantidad-d.facturada,facturar:d.cantidad-d.facturada};});
    if(!this.invoiceLines.length)throw new Error('Todas las cantidades de este despacho ya fueron facturadas.');
    this.currencies=[...new Set<string>(this.invoiceLines.map(l=>l.moneda))];this.invoiceDraft={despachoId:row.id,moneda:this.currencies[0],fechaEmision:this.today()};
  });}
  get invoiceValid():boolean{const rows=this.invoiceLines.filter(l=>l.moneda===this.invoiceDraft?.moneda);return rows.some(l=>l.facturar>0)&&rows.every(l=>Number.isInteger(l.facturar)&&l.facturar>=0&&l.facturar<=l.pendiente);}
  createInvoice():Promise<void>{return this.task(async()=>{
    const body={...this.invoiceDraft,detalles:this.invoiceLines.filter(l=>l.moneda===this.invoiceDraft.moneda&&l.facturar>0).map(l=>({detalleId:l.id,cantidad:l.facturar}))};
    await this.api.write('POST','facturas',body);this.invoiceDraft=null;await this.refresh();this.tab='facturas';this.api.success('Factura registrada. Su PDF interno está disponible.');
  });}
  guide(row:any):void{
    this.error='';this.mode='guia';this.target=row;this.model={despachoId:row.id,fechaTraslado:this.today()};
    this.fields=[{key:'fechaTraslado',label:'Fecha de traslado',type:'date',required:true}];
  }
  official(row:any):void{
    this.error='';this.mode='oficial';this.target={...row,resource:this.tab};this.model={serie:'',numero:'',archivo:''};
    this.fields=[{key:'serie',label:'Serie oficial',required:true,max:20},{key:'numero',label:'Número oficial',required:true,max:30},{key:'archivo',label:'Documento oficial',type:'file',required:true,wide:true}];
  }
  save():Promise<void>{return this.task(async()=>{
    await this.api.write('POST',this.mode==='guia'?'guias-remision':`${this.target.resource}/${this.target.id}/documento-oficial`,this.model);
    this.model=null;await this.refresh();this.api.success('Documento registrado correctamente');
  });}
  pdf(row:any):Promise<void>{return this.task(()=>this.api.file(`${this.tab}/${row.id}/pdf`,`${this.tab}-${row.id}.pdf`));}
}
