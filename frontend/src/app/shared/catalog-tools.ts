export interface CatalogFilters {
  query:string;category:number|'';origin:string;status:string;stock:string;
  dateField:string;from:string;to:string;level:string;parent:number|'';
}
export const newCatalogFilters=():CatalogFilters=>({query:'',category:'',origin:'',status:'',stock:'',dateField:'fechaCreacion',from:'',to:'',level:'',parent:''});
export const normalize=(value:unknown):string=>String(value??'').normalize('NFD').replace(/\p{Diacritic}/gu,'').toLowerCase().trim();
export function matchesSearch(text:string,query:string):boolean {
  const haystack=normalize(text);return normalize(query).split(/\s+/).filter(Boolean).every(word=>haystack.includes(word));
}
export function categoryIds(categories:any[],id:number):Set<number> {
  const ids=new Set<number>([id]);let changed=true;
  while(changed){changed=false;for(const c of categories)if(ids.has(c.padreId)&&!ids.has(c.id)){ids.add(c.id);changed=true;}}
  return ids;
}
export function localDate(value:string|null|undefined):string {
  if(!value)return '';if(/^\d{4}-\d{2}-\d{2}$/.test(value))return value;
  const date=new Date(value);if(Number.isNaN(date.getTime()))return '';
  const parts=new Intl.DateTimeFormat('en-CA',{timeZone:'America/Lima',year:'numeric',month:'2-digit',day:'2-digit'}).formatToParts(date);
  const part=(type:string)=>parts.find(p=>p.type===type)?.value||'';
  return `${part('year')}-${part('month')}-${part('day')}`;
}
export function inDateRange(value:string|null|undefined,from:string,to:string):boolean {
  if(!from&&!to)return true;const date=localDate(value);return !!date&&(!from||date>=from)&&(!to||date<=to);
}
export function stockMatches(items:any[],level:string):boolean {
  if(!level)return true;
  if(level==='sin-presentacion')return items.length===0;
  if(!items.length)return false;
  if(level==='sin-stock')return items.every(p=>Number(p.stockDisponible)===0);
  if(level==='reservado')return items.some(p=>Number(p.stockReservado)>0);
  if(level==='bajo')return items.some(p=>Number(p.stockDisponible)>0&&Number(p.stockDisponible)<Number(p.stockMinimo));
  if(level==='alto')return items.some(p=>Number(p.stockActual)>Number(p.stockAlto));
  if(level==='estable')return items.some(p=>Number(p.stockDisponible)>0&&Number(p.stockDisponible)>=Number(p.stockMinimo)&&Number(p.stockActual)<=Number(p.stockAlto));
  return items.some(p=>Number(p.stockDisponible)>0);
}
export function exportTable(name:string,headers:string[],rows:unknown[][]):void {
  const cell=(value:unknown)=>{let text=String(value??'');if(typeof value==='string'&&/^[=+@-]/.test(text))text="'"+text;return '"'+text.replaceAll('"','""')+'"';};
  const csv=[headers.map(cell).join(';'),...rows.map(row=>row.map(cell).join(';'))].join('\r\n');
  const url=URL.createObjectURL(new Blob(['\uFEFF'+csv],{type:'text/csv;charset=utf-8;'}));
  const a=document.createElement('a');a.href=url;a.download=name;a.click();setTimeout(()=>URL.revokeObjectURL(url),10000);
}
