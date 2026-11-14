import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PageState } from '../../shared/page-state';
import { Editor, FormField } from '../../shared/editor';
import { Icon } from '../../shared/icon';

@Component({selector:'app-returns',imports:[CommonModule,FormsModule,Editor,Icon],templateUrl:'./returns.html'})
export class Returns extends PageState implements OnInit {
  rows:any[]=[];invoices:any[]=[];draft:any=null;lines:any[]=[];model:any=null;target:any=null;selected:any=null;
  fields:FormField[]=[{key:'referencia',label:'Referencia del reembolso realizado',required:true,max:150},{key:'comprobante',label:'Comprobante del reembolso',type:'file',required:true,wide:true}];
  ngOnInit():void{void this.load();}
  private async refresh():Promise<void>{[this.rows,this.invoices]=await Promise.all([this.api.all('devoluciones'),this.api.all('facturas')]);}
  load():Promise<void>{return this.task(()=>this.refresh());}
  open():void{this.error='';this.lines=[];this.draft={facturaId:null,tratamiento:'REDUCIR_DEUDA',motivo:''};}
  invoiceChanged():Promise<void>{return this.task(async()=>{
    this.lines=[];if(!this.draft.facturaId)return;
    const f=await this.api.get(`facturas/${this.draft.facturaId}`);const p=await this.api.get(`pedidos/${f.factura.pedidoId}`);
    this.lines=f.detalles.filter((d:any)=>d.cantidad>d.devuelta).map((d:any)=>({...d,descripcion:p.detalles.find((l:any)=>l.id===d.detallePedidoId)?.descripcion||'Artículo',disponible:d.cantidad-d.devuelta,cantidadDevolver:0,apta:0}));
  });}
  get valid():boolean{return this.lines.some(l=>l.cantidadDevolver>0)&&this.lines.every(l=>Number.isInteger(l.cantidadDevolver)&&Number.isInteger(l.apta)&&l.cantidadDevolver>=0&&l.cantidadDevolver<=l.disponible&&l.apta>=0&&l.apta<=l.cantidadDevolver);}
  save():Promise<void>{return this.task(async()=>{
    const body={...this.draft,detalles:this.lines.filter(l=>l.cantidadDevolver>0).map(l=>({detalleFacturaId:l.id,cantidad:l.cantidadDevolver,apta:l.apta}))};
    await this.api.write('POST','devoluciones',body);this.draft=null;await this.refresh();this.api.success('Devolución registrada y existencias actualizadas');
  });}
  view(row:any):Promise<void>{return this.task(async()=>{this.selected=await this.api.get(`devoluciones/${row.id}`);});}
  refund(row:any):void{this.target=row;this.error='';this.model={referencia:'',comprobante:''};}
  confirmRefund():Promise<void>{return this.task(async()=>{
    await this.api.write('POST',`devoluciones/${this.target.id}/reembolso`,this.model);this.model=null;await this.refresh();this.api.success('Reembolso registrado');
  });}
}
