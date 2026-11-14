import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { PageState } from '../../shared/page-state';
import { Editor, FormField } from '../../shared/editor';
import { Icon } from '../../shared/icon';

@Component({selector:'app-orders',imports:[CommonModule,FormsModule,RouterLink,Editor,Icon],templateUrl:'./orders.html'})
export class Orders extends PageState implements OnInit {
  private readonly route=inject(ActivatedRoute);
  rows:any[]=[];clients:any[]=[];articles:any[]=[];addresses:any[]=[];page=0;pages=0;total=0;
  detail:any=null;draft:any=null;editing:number|null=null;action:any=null;actionModel:any=null;fields:FormField[]=[];
  shipment:any=null;shippingLines:any[]=[];
  ngOnInit():void{void this.task(async()=>{
    [this.clients,this.articles]=await Promise.all([this.api.all('clientes'),this.api.all('presentaciones')]);await this.refresh();
    const id=this.route.snapshot.queryParamMap.get('id');if(id)this.detail=await this.api.get(`pedidos/${id}`);
    else if(this.route.snapshot.queryParamMap.has('nuevo'))this.open();
  });}
  private async refresh():Promise<void>{const r=await this.api.page('pedidos',this.page);this.rows=r.content;this.pages=r.totalPages;this.total=r.totalElements;}
  loadPage(offset:number):Promise<void>{return this.task(async()=>{this.page+=offset;await this.refresh();});}
  view(id:number):Promise<void>{return this.task(async()=>{this.detail=await this.api.get(`pedidos/${id}`);});}
  open(existing?:any):void{
    this.error='';this.editing=existing?.pedido.id||null;
    this.draft=existing?{clienteId:existing.pedido.clienteId,direccionId:null,observaciones:existing.pedido.observaciones||'',credito:existing.pedido.credito,diasCredito:existing.pedido.diasCredito,tasaIgv:Number(existing.pedido.tasaIgv),motivo:'',detalles:existing.detalles.map((l:any)=>({presentacionId:l.presentacionId,cantidad:l.cantidad,precioSinIgv:Number(l.precioSinIgv),moneda:l.moneda,bonificacion:l.bonificacion}))}:{clienteId:null,direccionId:null,observaciones:'',credito:true,diasCredito:30,tasaIgv:0.18,motivo:null,detalles:[this.newLine()]};
    this.addresses=[];if(this.draft.clienteId)void this.clientChanged();
  }
  newLine():any{return{presentacionId:null,cantidad:1,precioSinIgv:0,moneda:'PEN',bonificacion:false};}
  clientChanged():Promise<void>{return this.task(async()=>{this.draft.direccionId=null;this.addresses=this.draft.clienteId?await this.api.get(`clientes/${this.draft.clienteId}/direcciones`):[];});}
  articleChanged(line:any):void{const p=this.articles.find(a=>a.id===line.presentacionId);if(p){line.precioSinIgv=line.bonificacion?0:Number(p.precioSinIgv);line.moneda=p.moneda;}}
  bonusChanged(line:any):void{if(line.bonificacion)line.precioSinIgv=0;else this.articleChanged(line);}
  lineSubtotal(line:any):number{
    const amount=line.bonificacion?0:Number(line.cantidad)*Number(line.precioSinIgv);
    return Number.isFinite(amount)?Math.round((amount+Number.EPSILON)*100)/100:0;
  }
  get preview():{moneda:string;subtotal:number;igv:number;total:number}[]{
    const totals=new Map<string,{moneda:string;subtotal:number;igv:number;total:number}>();
    for(const line of this.draft?.detalles||[]){
      const base=this.lineSubtotal(line);
      const tax=Math.round((base*(Number(this.draft.tasaIgv)||0)+Number.EPSILON)*100)/100;
      const value=totals.get(line.moneda)||{moneda:line.moneda,subtotal:0,igv:0,total:0};
      value.subtotal+=base;value.igv+=tax;value.total+=base+tax;totals.set(line.moneda,value);
    }
    return [...totals.values()];
  }
  save():Promise<void>{return this.task(async()=>{
    const body={...this.draft,diasCredito:this.draft.credito?this.draft.diasCredito:0};
    const result=await this.api.write(this.editing?'PUT':'POST',`pedidos${this.editing?'/'+this.editing:''}`,body);
    this.detail=result;this.draft=null;this.api.success(this.editing?'Pedido actualizado; vuelve a aprobarlo':'Pedido creado correctamente');await this.refresh();
  });}
  prepareAction(type:string):void{
    this.error='';this.action={type,id:this.detail.pedido.id};
    if(type==='aprobar'){this.actionModel={motivoExcepcion:''};this.fields=[{key:'motivoExcepcion',label:'Motivo de excepción de crédito (opcional)',type:'textarea',max:500,hint:'Solo complétalo si autorizas operar sobre el límite o con mora.'}];}
    else{this.actionModel={motivo:''};this.fields=[{key:'motivo',label:'Motivo de la operación',type:'textarea',required:true,max:500}];}
  }
  confirmAction():Promise<void>{return this.task(async()=>{
    this.detail=await this.api.write('POST',`pedidos/${this.action.id}/${this.action.type}`,this.actionModel);this.action=null;this.api.success('Pedido actualizado');await this.refresh();
  });}
  openShipment():void{
    const p=this.detail.pedido;this.error='';
    this.shipment={pedidoId:p.id,fechaDespacho:this.today(),destinatario:p.destinatario||'',documentoDestinatario:p.documentoDestinatario||'',direccion:p.direccionEnvio||'',agencia:p.agencia||'',recoge:'',costoEnvio:0,monedaEnvio:'PEN',pagaEnvio:'CLIENTE',conductor:'',vehiculo:'',observaciones:'',motivoExcepcion:''};
    this.shippingLines=this.detail.detalles.filter((l:any)=>l.reservada>0).map((l:any)=>({...l,enviar:l.reservada}));
  }
  get shippingValid():boolean{return this.shippingLines.some(l=>l.enviar>0)&&this.shippingLines.every(l=>Number.isInteger(l.enviar)&&l.enviar>=0&&l.enviar<=l.reservada);}
  dispatch():Promise<void>{return this.task(async()=>{
    if(!this.shippingValid)throw new Error('Revisa las cantidades a despachar.');
    const body={...this.shipment,detalles:this.shippingLines.filter(l=>l.enviar>0).map(l=>({detalleId:l.id,cantidad:l.enviar}))};
    await this.api.write('POST','despachos',body);this.shipment=null;
    this.detail=await this.api.get(`pedidos/${this.detail.pedido.id}`);await this.refresh();this.api.success('Despacho confirmado. Puedes facturarlo en Documentos.');
  });}
}
