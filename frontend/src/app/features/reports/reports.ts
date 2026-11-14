import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PageState } from '../../shared/page-state';
import { Icon } from '../../shared/icon';

interface Report { label:string;path:string;array:boolean;columns:{key:string;label:string}[]; }
@Component({selector:'app-reports',imports:[CommonModule,FormsModule,Icon],templateUrl:'./reports.html'})
export class Reports extends PageState implements OnInit {
  selected='stock';rows:any[]=[];days=7;
  readonly reports:Record<string,Report>={
    stock:{label:'Existencias y reservas',path:'reportes/stock',array:true,columns:[{key:'codigo',label:'Artículo'},{key:'stockActual',label:'Físico'},{key:'reservado',label:'Reservado'},{key:'disponible',label:'Disponible'},{key:'stockMinimo',label:'Mínimo'},{key:'stockAlto',label:'Alto'},{key:'alerta',label:'Estado'}]},
    vencimientos:{label:'Vencimientos y mora',path:'reportes/vencimientos',array:true,columns:[{key:'factura.id',label:'Factura'},{key:'factura.clienteNombre',label:'Cliente'},{key:'factura.moneda',label:'Moneda'},{key:'factura.saldo',label:'Saldo'},{key:'factura.fechaVencimiento',label:'Vencimiento'},{key:'diasDesdeInicioCredito',label:'Días desde inicio'},{key:'diasMora',label:'Días de mora'},{key:'diasParaVencer',label:'Días para vencer'}]},
    danados:{label:'Productos dañados',path:'reportes/productos-danados',array:false,columns:[{key:'detalleDevolucionId',label:'Detalle devolución'},{key:'presentacionId',label:'Presentación'},{key:'cantidadDanada',label:'Dañados'}]},
    auditoria:{label:'Auditoría de operaciones',path:'auditoria',array:false,columns:[{key:'creadoEn',label:'Fecha'},{key:'usuarioCreador',label:'Usuario'},{key:'operacion',label:'Operación'},{key:'recurso',label:'Referencia'},{key:'motivo',label:'Motivo'}]},
    transferencias:{label:'Transferencias de deuda',path:'transferencias-deuda',array:false,columns:[{key:'facturaId',label:'Factura'},{key:'vendedorAnteriorId',label:'Responsable anterior'},{key:'vendedorNuevoId',label:'Responsable nuevo'},{key:'saldoTransferido',label:'Saldo transferido'},{key:'motivo',label:'Motivo'},{key:'usuarioCreador',label:'Usuario'}]}
  };
  get report():Report{return this.reports[this.selected];}
  ngOnInit():void{void this.load();}
  load():Promise<void>{return this.task(async()=>{this.rows=[];this.rows=this.report.array?await this.api.get(this.report.path+(this.selected==='vencimientos'?`?diasAviso=${this.days}`:'')):await this.api.all(this.report.path);});}
  value(row:any,key:string):any{return key.split('.').reduce((value,part)=>value?.[part],row)??'—';}
  exportCsv():void{
    const escape=(value:any)=>{let text=String(value??'');if(typeof value==='string'&&/^[=+@-]/.test(text))text="'"+text;return '"'+text.replaceAll('"','""')+'"';};
    const csv=[this.report.columns.map(c=>escape(c.label)).join(';'),...this.rows.map(r=>this.report.columns.map(c=>escape(this.value(r,c.key))).join(';'))].join('\r\n');
    const url=URL.createObjectURL(new Blob(['\uFEFF'+csv],{type:'text/csv;charset=utf-8;'}));const a=document.createElement('a');a.href=url;a.download=`biofer-${this.selected}-${this.today()}.csv`;a.click();setTimeout(()=>URL.revokeObjectURL(url),10000);
  }
}
