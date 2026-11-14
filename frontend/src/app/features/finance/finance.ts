import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PageState } from '../../shared/page-state';
import { Editor, FormField } from '../../shared/editor';
import { Icon } from '../../shared/icon';

@Component({selector:'app-finance',imports:[CommonModule,FormsModule,Editor,Icon],templateUrl:'./finance.html'})
export class Finance extends PageState implements OnInit {
  tab='pagos';payments:any[]=[];credits:any[]=[];sellers:any[]=[];invoices:any[]=[];
  model:any=null;fields:FormField[]=[];mode='';target:any=null;apply:any=null;allocations:any[]=[];
  sellerId:number|null=null;account:any=null;balances:any=null;
  ngOnInit():void{void this.load();}
  private async refresh():Promise<void>{[this.payments,this.credits,this.sellers,this.invoices]=await Promise.all([this.api.all('pagos'),this.api.all('creditos'),this.api.all('vendedores'),this.api.all('facturas')]);if(this.sellerId)await this.refreshAccount();}
  load():Promise<void>{return this.task(()=>this.refresh());}
  sellerName(id:number):string{return this.personName(this.sellers.find(s=>s.id===id));}
  prepare(mode:string,row?:any):void{
    this.mode=mode;this.target=row;this.error='';
    const sellers=this.sellers.filter(s=>s.vigente).map(s=>({value:s.id,label:this.personName(s)}));
    if(mode==='pago'){
      this.model={vendedorId:null,moneda:'PEN',monto:null,metodo:'TRANSFERENCIA',referencia:'',comprobante:'',fechaPago:this.today()};
      this.fields=[{key:'vendedorId',label:'Vendedor responsable',type:'select',required:true,options:sellers},{key:'moneda',label:'Moneda',required:true,max:3},{key:'monto',label:'Monto recibido',type:'number',min:0.01,step:'0.01',required:true},{key:'metodo',label:'Método de pago',type:'select',required:true,options:['TRANSFERENCIA','DEPOSITO','EFECTIVO','YAPE','PLIN','OTRO'].map(v=>({value:v,label:v}))},{key:'referencia',label:'Referencia única de la operación',required:true,max:150},{key:'fechaPago',label:'Fecha de pago',type:'date',required:true},{key:'comprobante',label:'Soporte del pago',type:'file',required:true,wide:true}];
    }else if(mode==='credito'){
      this.model={vendedorId:row?.vendedorId||null,moneda:row?.moneda||'PEN',limite:row?.limite??null,aprobado:row?.aprobado??true,motivo:''};
      this.fields=[{key:'vendedorId',label:'Vendedor',type:'select',required:true,options:sellers},{key:'moneda',label:'Moneda',required:true,max:3},{key:'limite',label:'Límite de crédito',type:'number',min:0,step:'0.01',required:true},{key:'aprobado',label:'Crédito aprobado',type:'checkbox'},{key:'motivo',label:'Motivo de la autorización o cambio',type:'textarea',required:true,max:500}];
    }else if(mode==='transferir'){
      this.model={vendedorNuevoId:null,motivo:'',motivoExcepcion:''};
      this.fields=[{key:'vendedorNuevoId',label:'Nuevo responsable de la deuda',type:'select',required:true,options:sellers.filter(s=>s.value!==row.vendedorId)},{key:'motivo',label:'Motivo de transferencia',type:'textarea',required:true,max:500},{key:'motivoExcepcion',label:'Excepción de crédito (solo si la autorizas)',type:'textarea',max:500}];
    }else if(mode==='confirmar'){
      this.model={};this.fields=[];
    }else{
      this.model={motivo:''};this.fields=[{key:'motivo',label:'Motivo',type:'textarea',required:true,max:500}];
    }
  }
  get title():string{return({pago:'Registrar pago',credito:'Configurar crédito',confirmar:'Confirmar recepción del pago',rechazar:'Rechazar pago',revertir:'Revertir pago',transferir:'Transferir deuda pendiente'} as Record<string,string>)[this.mode]||'Actualizar';}
  save():Promise<void>{return this.task(async()=>{
    if(this.mode==='pago')await this.api.write('POST','pagos',this.model);
    else if(this.mode==='credito')await this.api.write('PUT','creditos',this.model);
    else if(this.mode==='transferir')await this.api.write('POST',`facturas/${this.target.id}/transferir-deuda`,this.model);
    else await this.api.write('POST',`pagos/${this.target.id}/${this.mode}`,this.model);
    this.model=null;this.api.success('Operación registrada correctamente');await this.refresh();
  });}
  openApply(row:any,type='pago'):void{
    this.error='';this.apply={...row,type};
    this.allocations=this.invoices.filter(f=>f.saldo>0&&f.vendedorId===row.vendedorId&&f.moneda===row.moneda).map(f=>({...f,monto:0}));
  }
  get applyingTotal():number{return this.allocations.reduce((n,f)=>n+Number(f.monto||0),0);}
  get applyValid():boolean{return this.applyingTotal>0&&this.applyingTotal<=Number(this.apply?.disponible)+0.000001&&this.allocations.every(f=>f.monto>=0&&f.monto<=f.saldo);}
  applyFunds():Promise<void>{return this.task(async()=>{
    const body={detalles:this.allocations.filter(f=>f.monto>0).map(f=>({facturaId:f.id,monto:f.monto}))};
    await this.api.write('POST',`${this.apply.type==='saldo'?'saldos-favor':'pagos'}/${this.apply.id}/aplicaciones`,body);
    this.apply=null;await this.refresh();this.api.success('Importes aplicados a las deudas');
  });}
  private async refreshAccount():Promise<void>{[this.account,this.balances]=await Promise.all([this.api.get(`vendedores/${this.sellerId}/cuenta-corriente`),this.api.get(`vendedores/${this.sellerId}/saldos-favor`)]);}
  accountChanged():Promise<void>{return this.task(async()=>{this.account=null;this.balances=null;if(this.sellerId)await this.refreshAccount();});}
}
