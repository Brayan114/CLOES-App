import React from 'react';
import { C } from '../utils';
import { Btn } from './Shared';

const NAV = [
  {id:'dashboard',label:'Dashboard',icon:'◈'},
  {id:'users',    label:'Users',    icon:'◉'},
  {id:'messages', label:'Messages', icon:'💬'},
  {id:'vibes',    label:'Vibes',    icon:'▶'},
  {id:'coins',    label:'Coins',    icon:'🪙'},
  {id:'bloom',    label:'Bloom',    icon:'🌸'},
  {id:'settings', label:'Settings', icon:'⚙'},
];

export default function Sidebar({page,setPage,admin,logout}) {
  return (
    <nav style={{width:226,background:C.surface,borderRight:`1px solid ${C.border}`,display:'flex',flexDirection:'column',position:'fixed',top:0,left:0,bottom:0,zIndex:50,overflow:'hidden'}}>
      <div style={{padding:'22px 18px 16px',borderBottom:`1px solid ${C.border}`,flexShrink:0}}>
        <div style={{fontSize:22,fontWeight:800,background:`linear-gradient(135deg,${C.purple},${C.pink})`,WebkitBackgroundClip:'text',WebkitTextFillColor:'transparent',letterSpacing:6,fontFamily:"'Space Mono',monospace"}}>CLOES</div>
        <div style={{fontSize:9,color:C.sub,letterSpacing:'0.18em',marginTop:2}}>ADMIN PORTAL v2.0</div>
      </div>

      <div style={{flex:1,padding:'8px',display:'flex',flexDirection:'column',gap:2,overflowY:'auto'}}>
        {NAV.map(n => (
          <button key={n.id} onClick={()=>setPage(n.id)} style={{
            background:page===n.id?`${C.purple}20`:'transparent',
            border:page===n.id?`1px solid ${C.purple}3A`:'1px solid transparent',
            borderRadius:10,padding:'10px 12px',
            color:page===n.id?C.text:C.sub,
            cursor:'pointer',display:'flex',alignItems:'center',gap:10,
            fontFamily:'inherit',fontSize:13,fontWeight:page===n.id?600:400,textAlign:'left',width:'100%',
          }}>
            <span style={{fontSize:14,width:20,textAlign:'center',flexShrink:0}}>{n.icon}</span>
            {n.label}
            {page===n.id && <div style={{marginLeft:'auto',width:4,height:4,borderRadius:2,background:C.purple}}/>}
          </button>
        ))}
      </div>

      <div style={{padding:'12px',borderTop:`1px solid ${C.border}`,flexShrink:0}}>
        <div style={{display:'flex',alignItems:'center',gap:9,marginBottom:10}}>
          <div style={{width:32,height:32,borderRadius:9,background:`linear-gradient(135deg,${C.purple},${C.pink})`,display:'flex',alignItems:'center',justifyContent:'center',fontSize:13,fontWeight:700,flexShrink:0}}>{admin?.name?.[0]||'A'}</div>
          <div style={{minWidth:0}}>
            <div style={{fontSize:13,fontWeight:600,overflow:'hidden',textOverflow:'ellipsis',whiteSpace:'nowrap'}}>{admin?.name}</div>
            <div style={{fontSize:10,color:C.sub}}>Super Admin</div>
          </div>
        </div>
        <Btn ghost onClick={logout} style={{width:'100%',fontSize:12,padding:'7px'}}>Sign Out</Btn>
      </div>
    </nav>
  );
}
