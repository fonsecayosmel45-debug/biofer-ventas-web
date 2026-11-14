import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PageState } from '../../shared/page-state';
import { Editor, FormField } from '../../shared/editor';
import { Icon } from '../../shared/icon';

@Component({ selector:'app-people',imports:[CommonModule,FormsModule,Editor,Icon],templateUrl:'./people.html' })
export class People extends PageState implements OnInit {
  readonly resource=inject(ActivatedRoute).snapshot.data['resource'] as string;
  get isClient():boolean{return this.resource==='clientes';}
  rows:any[]=[];sellers:any[]=[];search='';filterSearch='';model:any=null;fields:FormField[]=[];editing:number|null=null;mode='contact';readOnlyMode=false;
  filters:{type:string;status:string;seller:number|null}={type:'',status:'',seller:null};
  applied:{type:string;status:string;seller:number|null}={...this.filters};
  page=1;
  contact:any=null;children:any[]=[];account:any=null;
  ngOnInit():void{void this.load();}
  private async refresh():Promise<void>{this.rows=await this.api.all(this.resource);if(this.isClient)this.sellers=await this.api.all('vendedores');}
  load():Promise<void>{return this.task(()=>this.refresh());}
  get filtered():any[]{const text=this.search.toLowerCase();const f=this.applied;return this.rows.filter(p=>`${this.personName(p)} ${p.datos.numeroDocumento} ${p.datos.email||''}`.toLowerCase().includes(text)&&(!f.type||p.datos.tipoPersona===f.type)&&(!f.status||p.datos.activo===(f.status==='activo'))&&(!f.seller||p.vendedorId===f.seller));}
  get activeCount():number{return this.rows.filter(p=>p.datos.activo).length;}
  get peopleCount():number{return this.rows.filter(p=>p.datos.tipoPersona==='NATURAL').length;}
  get companyCount():number{return this.rows.filter(p=>p.datos.tipoPersona==='JURIDICA').length;}
  get pages():number{return Math.max(1,Math.ceil(this.filtered.length/15));}
  get currentPage():number{return Math.min(this.page,this.pages);}
  get visibleRows():any[]{return this.filtered.slice((this.currentPage-1)*15,this.currentPage*15);}
  applyFilters():void{this.search=this.filterSearch;this.applied={...this.filters};this.page=1;}
  clearFilters():void{this.filterSearch='';this.filters={type:'',status:'',seller:null};this.applyFilters();}
  open(row?:any,readOnly=false):void{
    this.error='';this.mode='contact';this.editing=row?.id||null;this.readOnlyMode=readOnly;
    this.model={tipoPersona:'NATURAL',nombres:'',apellidos:'',razonSocial:'',nombreComercial:'',tipoDocumento:'DNI',numeroDocumento:'',telefono:'',email:'',activo:true,...row?.datos,
      vendedorId:row?.vendedorId||null,genero:row?.genero||'',metodoEnvio:row?.metodoEnvio||'AGENCIA',transportePreferido:row?.transportePreferido||'',
      fechaNacimiento:row?.fechaNacimiento||null,telefonoAlternativo:row?.telefonoAlternativo||'',direccion:row?.direccion||'',departamento:row?.departamento||'',provincia:row?.provincia||'',ciudad:row?.ciudad||'',monedaPago:row?.monedaPago||'PEN',fechaCese:row?.fechaCese||null,motivoCambioVendedor:''};
    this.fields=[{key:'tipoPersona',label:'Tipo de persona',type:'select',required:true,options:[{value:'NATURAL',label:'Persona natural'},{value:'JURIDICA',label:'Persona jurídica / empresa'}]},
      {key:'tipoDocumento',label:'Tipo de documento',type:'select',required:true,options:['DNI','RUC','CE','PASAPORTE'].map(v=>({value:v,label:v}))},
      {key:'numeroDocumento',label:'Número de documento',required:true,max:30},{key:'nombres',label:'Nombres (persona natural)',max:100},{key:'apellidos',label:'Apellidos (persona natural)',max:150},{key:'razonSocial',label:'Razón social (empresa)',max:200},{key:'nombreComercial',label:'Nombre comercial',max:150},{key:'telefono',label:'Teléfono',max:30},{key:'email',label:'Correo electrónico',type:'email',max:150}];
    if(this.isClient)this.fields.push({key:'vendedorId',label:'Vendedor responsable',type:'select',required:true,options:this.sellers.filter(v=>v.vigente||v.id===row?.vendedorId).map(v=>({value:v.id,label:this.personName(v)}))},{key:'genero',label:'Género',type:'select',options:[{value:'FEMENINO',label:'Femenino'},{value:'MASCULINO',label:'Masculino'},{value:'OTRO',label:'Otro'}]},{key:'metodoEnvio',label:'Método de envío',max:100},{key:'transportePreferido',label:'Transporte de preferencia',max:150},...(row?[{key:'motivoCambioVendedor',label:'Motivo de cambio de vendedor',type:'textarea' as const,max:500,hint:'Obligatorio si cambias el responsable. No transfiere deudas previas.'}]:[]));
    else this.fields.push({key:'fechaNacimiento',label:'Fecha de nacimiento',type:'date'},{key:'telefonoAlternativo',label:'Teléfono alternativo',max:30},{key:'direccion',label:'Dirección',max:250},{key:'departamento',label:'Departamento',max:100},{key:'provincia',label:'Provincia',max:100},{key:'ciudad',label:'Ciudad',max:100},{key:'monedaPago',label:'Moneda de pago',required:true,max:3},{key:'fechaCese',label:'Fecha de cese (opcional)',type:'date'});
    this.fields.push({key:'activo',label:'Registro activo',type:'checkbox'});
  }
  showChildren(row:any):Promise<void>{return this.task(async()=>{this.contact=row;this.children=await this.api.get(`${this.resource}/${row.id}/${this.isClient?'direcciones':'cuentas-bancarias'}`);});}
  openChild(row?:any):void{
    this.error='';this.mode='child';this.editing=row?.id||null;this.readOnlyMode=false;
    if(this.isClient){
      this.model={destinatario:this.personName(this.contact),documentoDestinatario:this.contact.datos.numeroDocumento,direccion:'',departamento:'',provincia:'',distrito:'',ciudad:'',referencia:'',principal:!this.children.some(d=>d.principal&&d.activa),activa:true,...row};
      this.fields=[{key:'destinatario',label:'Destinatario',required:true,max:200},{key:'documentoDestinatario',label:'Documento del destinatario',max:30},{key:'direccion',label:'Dirección',required:true,max:250},{key:'departamento',label:'Departamento',required:true,max:100},{key:'provincia',label:'Provincia',required:true,max:100},{key:'distrito',label:'Distrito',required:true,max:100},{key:'ciudad',label:'Ciudad',max:100},{key:'referencia',label:'Referencia',max:250},{key:'principal',label:'Dirección principal',type:'checkbox'},{key:'activa',label:'Dirección activa',type:'checkbox'}];
    }else{
      this.model={banco:'',numeroCuenta:'',cci:null,titular:this.personName(this.contact),moneda:'PEN',activa:true,...row};
      this.fields=[{key:'banco',label:'Banco',required:true,max:100},{key:'numeroCuenta',label:'Número de cuenta',required:true,max:40},{key:'cci',label:'CCI (20 dígitos, opcional)',max:20},{key:'titular',label:'Titular',required:true,max:200},{key:'moneda',label:'Moneda',required:true,max:3},{key:'activa',label:'Cuenta activa',type:'checkbox'}];
    }
  }
  save():Promise<void>{return this.task(async()=>{
    if(this.mode==='child'){
      const path=`${this.resource}/${this.contact.id}/${this.isClient?'direcciones':'cuentas-bancarias'}`;
      const body:any=Object.fromEntries(this.fields.map(field=>[field.key,this.model[field.key]]));if(!this.isClient&&!body.cci)body.cci=null;
      await this.api.write(this.editing?'PUT':'POST',`${path}${this.editing?'/'+this.editing:''}`,body,false);
      this.children=await this.api.get(path);
    }else{
      const m=this.model;const datos={tipoPersona:m.tipoPersona,nombres:m.nombres||null,apellidos:m.apellidos||null,razonSocial:m.razonSocial||null,nombreComercial:m.nombreComercial||null,tipoDocumento:m.tipoDocumento,numeroDocumento:m.numeroDocumento,telefono:m.telefono||null,email:m.email||null,activo:m.activo};
      const body=this.isClient?{datos,genero:m.genero||null,vendedorId:m.vendedorId,metodoEnvio:m.metodoEnvio,transportePreferido:m.transportePreferido,motivoCambioVendedor:m.motivoCambioVendedor||null}:{datos,fechaNacimiento:m.fechaNacimiento||null,telefonoAlternativo:m.telefonoAlternativo||null,direccion:m.direccion,departamento:m.departamento,provincia:m.provincia,ciudad:m.ciudad,monedaPago:m.monedaPago.toUpperCase(),fechaCese:m.fechaCese||null};
      await this.api.write(this.editing?'PUT':'POST',`${this.resource}${this.editing?'/'+this.editing:''}`,body,false);await this.refresh();
    }
    this.model=null;this.api.success('Datos guardados correctamente');
  });}
  showAccount(row:any):Promise<void>{return this.task(async()=>{
    const data=await this.api.get(`${this.resource}/${row.id}/cuenta-corriente`);this.account={...data,name:this.personName(row)};
  });}
}
