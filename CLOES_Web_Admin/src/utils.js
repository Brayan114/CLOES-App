export const C = {
  bg:'#0B0715', surface:'#130E22', s2:'#1A1130', border:'#2D1F4A',
  purple:'#8B5CF6', pink:'#FF3385', cyan:'#33A1FF', mint:'#4DFFD4',
  gold:'#F59E0B', red:'#EF4444', green:'#22C55E',
  text:'#F0E8FF', mid:'#C4B0E8', sub:'#7A6A9A',
};
export const PAL = [C.purple,C.pink,C.cyan,C.mint,C.gold,C.green];

export let TOKEN   = localStorage.getItem('cloes_admin_token') || '';
export let API_URL = localStorage.getItem('cloes_admin_api')   || 'https://cloes-app-production.up.railway.app/api';

export function setToken(t) { 
  TOKEN = t; 
  if(!t) localStorage.removeItem('cloes_admin_token'); 
  else localStorage.setItem('cloes_admin_token', t); 
}
export function setApiUrl(u) { 
  API_URL = u; 
  localStorage.setItem('cloes_admin_api', u); 
}

export async function req(method, path, body) {
  const r = await fetch(API_URL + path, {
    method,
    headers: { 'Content-Type':'application/json', Authorization:`Bearer ${TOKEN}` },
    body: body ? JSON.stringify(body) : undefined,
  });
  const d = await r.json().catch(()=>({}));
  if (!r.ok) throw new Error(d.error || `HTTP ${r.status}`);
  return d;
}

export const fmt   = n => n>=1e6?(n/1e6).toFixed(1)+'M':n>=1e3?(n/1e3).toFixed(1)+'K':String(n||0);
export const dstr  = d => new Date(d).toLocaleDateString('en',{month:'short',day:'numeric',year:'numeric'});
export const tstr  = d => new Date(d).toLocaleString('en',{month:'short',day:'numeric',hour:'2-digit',minute:'2-digit'});
export const week  = data => {
  if (!data) return [];
  const m={}; data.forEach(d=>{m[d._id]=d.count});
  return Array.from({length:7},(_,i)=>{
    const dt=new Date(Date.now()-(6-i)*86400000);
    return {day:dt.toLocaleDateString('en',{weekday:'short'}), count:m[dt.toISOString().split('T')[0]]||0};
  });
};
