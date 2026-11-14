import { Component, input, output, HostListener, inject, signal } from '@angular/core';
import { ApiService, errorMessage } from '../core/api.service';
import { FormsModule } from '@angular/forms';
import { Icon } from './icon';

export interface Option { value: any; label: string; }
export interface FormField {
  key: string; label: string; type?: 'text' | 'email' | 'number' | 'date' | 'select' | 'multi' | 'textarea' | 'checkbox' | 'file';
  required?: boolean; options?: Option[]; min?: number; step?: string; hint?: string; wide?: boolean; max?: number;
}
@Component({
  selector: 'app-editor', imports: [FormsModule, Icon], templateUrl: './editor.html'
})
export class Editor {
  private readonly api=inject(ApiService);
  readonly uploading=signal(false); readonly uploadError=signal('');
  title = input.required<string>(); model = input.required<any>(); fields = input.required<FormField[]>();
  busy = input(false); readOnly = input(false); error = input(''); subtitle = input('Completa los datos para continuar.');
  saved = output<void>(); closed = output<void>();
  @HostListener('document:keydown.escape') escape(): void { if (!this.busy() && !this.uploading()) this.closed.emit(); }
  async upload(event:Event,key:string):Promise<void>{
    const file=(event.target as HTMLInputElement).files?.[0];if(!file)return;
    if(file.size>10*1024*1024){this.uploadError.set('El archivo no debe superar 10 MB.');return;}
    this.uploading.set(true);this.uploadError.set('');
    try{this.model()[key]=await this.api.upload(file);}catch(e){this.uploadError.set(errorMessage(e));}finally{this.uploading.set(false);}
  }
  visible(field:FormField):boolean {
    const type=this.model().tipoPersona;
    if(type==='JURIDICA'&&['nombres','apellidos','fechaNacimiento','genero'].includes(field.key))return false;
    if(type==='NATURAL'&&field.key==='razonSocial')return false;
    return true;
  }
  isRequired(field:FormField):boolean {
    if(this.model().tipoPersona==='NATURAL'&&['nombres','apellidos'].includes(field.key))return true;
    if(this.model().tipoPersona==='JURIDICA'&&field.key==='razonSocial')return true;
    return !!field.required;
  }
  fieldChanged(field:FormField):void {
    if(field.key!=='tipoPersona')return;
    const model=this.model();
    if(model.tipoPersona==='NATURAL'){
      model.razonSocial=null;if(model.tipoDocumento==='RUC')model.tipoDocumento='DNI';
    }else{
      model.nombres=null;model.apellidos=null;model.fechaNacimiento=null;model.genero=null;
      if(model.tipoDocumento==='DNI')model.tipoDocumento='RUC';
    }
  }
  sections():{title:string;icon:string;fields:FormField[]}[] {
    const groups=new Map<string,{title:string;icon:string;fields:FormField[]}>();
    for(const field of this.fields().filter(f=>this.visible(f))){
      const [title,icon]=this.sectionFor(field.key);
      if(!groups.has(title))groups.set(title,{title,icon,fields:[]});
      groups.get(title)!.fields.push(field);
    }
    return [...groups.values()];
  }
  private sectionFor(key:string):[string,string] {
    if(['activo','activa','aprobado'].includes(key))return ['Estado y autorización','check'];
    if(['comprobante','archivo','referencia','serie','numero','fechaPago'].includes(key))return ['Documentos y referencias','file'];
    if(['motivo','motivoCambioPrecio','motivoCambioVendedor','motivoExcepcion','descripcion'].includes(key))return ['Observaciones y trazabilidad','file'];
    if(['telefono','telefonoAlternativo','email'].includes(key))return ['Datos de contacto','users'];
    if(['direccion','departamento','provincia','distrito','ciudad','referenciaDireccion','destinatario','documentoDestinatario','principal','metodoEnvio','transportePreferido','fechaTraslado'].includes(key))return ['Ubicación y entrega','truck'];
    if(['precioSinIgv','moneda','stockMinimo','stockAlto','monto','metodo','limite','monedaPago','banco','numeroCuenta','cci','titular'].includes(key))return ['Datos comerciales','wallet'];
    if(['vendedorId','vendedorNuevoId','fechaCese'].includes(key))return ['Responsable comercial','users'];
    if(['tipo','cantidad'].includes(key))return ['Movimiento de inventario','box'];
    return ['Identificación del registro','leaf'];
  }
}
