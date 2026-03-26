import React, { useState } from 'react';
import { C } from '../utils';

export const Spin = ({s=28}) => <div className="spin" style={{width:s,height:s,border:`3px solid ${C.border}`,borderTop:`3px solid ${C.purple}`,borderRadius:'50%'}}/>;
export const Loader = () => <div style={{display:'flex',justifyContent:'center',padding:80}}><Spin s={36}/></div>;

export const Badge = ({text,c}) => (
  <span style={{background:`${c}22`,color:c,borderRadius:6,padding:'2px 9px',fontSize:11,fontWeight:700,letterSpacing:'0.04em',display:'inline-block'}}>{text}</span>
);

export const Inp = ({style,...p}) => (
  <input style={{background:C.s2,border:`1px solid ${C.border}`,borderRadius:10,padding:'9px 14px',color:C.text,fontSize:13,fontFamily:'inherit',width:'100%',...style}} {...p}/>
);

export const Lbl = ({children}) => <div style={{fontSize:11,color:C.sub,letterSpacing:'0.08em',textTransform:'uppercase',marginBottom:6}}>{children}</div>;

export const Btn = ({children,color=C.purple,ghost,style={},disabled,onClick}) => (
  <button onClick={onClick} disabled={disabled}
    style={{background:ghost?'transparent':color,border:ghost?`1px solid ${C.border}`:'none',
      borderRadius:10,padding:'9px 18px',color:ghost?C.mid:'#fff',fontWeight:600,
      fontSize:13,cursor:'pointer',fontFamily:'inherit',...style}}>
    {children}
  </button>
);

export const Card = ({children,style={}}) => (
  <div style={{background:C.surface,border:`1px solid ${C.border}`,borderRadius:16,padding:22,...style}}>{children}</div>
);

export const ST = ({children}) => (
  <div style={{fontSize:11,fontWeight:700,color:C.sub,letterSpacing:'0.1em',textTransform:'uppercase',marginBottom:14}}>{children}</div>
);

export const PH = ({title,sub}) => (
  <div style={{marginBottom:28}}>
    <h1 style={{fontSize:26,fontWeight:800,fontFamily:"'Space Mono',monospace",letterSpacing:1}}>{title}</h1>
    {sub && <p style={{color:C.sub,fontSize:13,marginTop:4}}>{sub}</p>}
  </div>
);

export const StatCard = ({label,value,sub,color,icon}) => (
  <div style={{background:C.surface,border:`1px solid ${C.border}`,borderRadius:16,padding:22,position:'relative',overflow:'hidden'}}>
    <div style={{position:'absolute',top:-20,right:-20,width:70,height:70,borderRadius:'50%',background:`${color}14`}}/>
    <div style={{fontSize:24,marginBottom:6}}>{icon}</div>
    <div style={{fontSize:30,fontWeight:800,color,fontFamily:"'Space Mono',monospace",lineHeight:1}}>{value}</div>
    <div style={{fontSize:13,color:C.sub,marginTop:6}}>{label}</div>
    {sub && <div style={{fontSize:11,color:`${color}BB`,marginTop:3}}>{sub}</div>}
  </div>
);

export const Modal = ({title,onClose,children}) => (
  <div onClick={onClose} style={{position:'fixed',inset:0,background:'rgba(0,0,0,0.75)',display:'flex',alignItems:'center',justifyContent:'center',zIndex:200}}>
    <div onClick={e=>e.stopPropagation()} className="fade" style={{background:C.surface,border:`1px solid ${C.border}`,borderRadius:18,padding:28,width:420,maxWidth:'90vw'}}>
      <div style={{display:'flex',justifyContent:'space-between',alignItems:'center',marginBottom:20}}>
        <div style={{fontSize:16,fontWeight:700}}>{title}</div>
        <button onClick={onClose} style={{background:'none',border:'none',color:C.sub,fontSize:20,cursor:'pointer'}}>✕</button>
      </div>
      {children}
    </div>
  </div>
);

export const Pager = ({page,total,limit,go}) => {
  const pages = Math.ceil(total/limit);
  if (pages<=1) return null;
  return (
    <div style={{display:'flex',gap:10,marginTop:20,justifyContent:'center',alignItems:'center'}}>
      <Btn ghost onClick={()=>go(p=>Math.max(1,p-1))} disabled={page===1} style={{padding:'7px 14px',fontSize:12}}>← Prev</Btn>
      <span style={{color:C.sub,fontSize:13}}>Page {page} / {pages} &nbsp;·&nbsp; {total.toLocaleString()} total</span>
      <Btn ghost onClick={()=>go(p=>Math.min(pages,p+1))} disabled={page===pages} style={{padding:'7px 14px',fontSize:12}}>Next →</Btn>
    </div>
  );
};

export const Toast = ({msg}) => msg ? (
  <div style={{position:'fixed',bottom:24,right:24,background:C.purple,color:'#fff',borderRadius:12,padding:'12px 20px',fontWeight:600,fontSize:13,zIndex:300,boxShadow:'0 4px 24px rgba(0,0,0,0.5)',animation:'fadeIn 0.2s ease'}}>
    {msg}
  </div>
) : null;

export const useToast = () => {
  const [msg,setMsg] = useState('');
  const show = m => { setMsg(m); setTimeout(()=>setMsg(''),2500); };
  return [msg, show];
};

export const TH = ({children}) => <th style={{padding:'10px 14px',textAlign:'left',fontSize:11,fontWeight:600,letterSpacing:'0.07em',textTransform:'uppercase',color:C.sub,borderBottom:`1px solid ${C.border}`,whiteSpace:'nowrap'}}>{children}</th>;
export const TD = ({children,style={}}) => <td style={{padding:'11px 14px',fontSize:13,color:C.mid,borderBottom:`1px solid ${C.border}20`,...style}}>{children}</td>;
