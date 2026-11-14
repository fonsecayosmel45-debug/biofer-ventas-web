import { Component, input, output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Option } from './editor';
import { Icon } from './icon';

export interface FilterField {
  key:string;label:string;type:'search'|'select'|'date'|'number';options?:Option[];
  advanced?:boolean;wide?:boolean;hint?:string;placeholder?:string;min?:number;
}
@Component({selector:'app-filter-panel',imports:[FormsModule,Icon],templateUrl:'./filter-panel.html'})
export class FilterPanel {
  model=input.required<any>();fields=input.required<FilterField[]>();busy=input(false);error=input('');count=input(0);
  applied=output<void>();cleared=output<void>();expanded=false;
  get hasAdvanced():boolean{return this.fields().some(f=>f.advanced);}
  get inventoryFields():boolean{return this.fields().some(f=>f.key==='product');}
}
