import React, { useState, useEffect } from 'react';
import { C, API_URL, req } from '../utils';

export default function Topbar() {
  const [ping,setPing] = useState(null);
  useEffect(()=>{
    const check = () => {
      const t = Date.now();
      req('GET','/health').then(()=>setPing(Date.now()-t)).catch(()=>setPing(-1));
    };
    check();
    const id = setInterval(check,30000);
    return ()=>clearInterval(id);
  },[]);
  
  const pcolor = ping===null?C.gold:ping===-1?C.red:C.green;
  const plabel = ping===null?'Connecting…':ping===-1?'Backend offline':`${ping}ms`;
  
  return (
    <div style={{background:C.surface,borderBottom:`1px solid ${C.border}`,padding:'12px 24px',display:'flex',alignItems:'center',justifyContent:'space-between',position:'sticky',top:0,zIndex:40}}>
      <div style={{fontSize:13,color:C.sub}}>{new Date().toLocaleDateString('en',{weekday:'long',year:'numeric',month:'long',day:'numeric'})}</div>
      <div style={{display:'flex',alignItems:'center',gap:14}}>
        <div style={{display:'flex',alignItems:'center',gap:7}}>
          <div style={{width:7,height:7,borderRadius:'50%',background:pcolor,boxShadow:`0 0 6px ${pcolor}`}}/>
          <span style={{fontSize:12,color:C.sub}}>{plabel}</span>
        </div>
        <div style={{fontFamily:"'Space Mono',monospace",fontSize:10,color:C.sub,background:C.s2,padding:'4px 10px',borderRadius:6,maxWidth:220,overflow:'hidden',textOverflow:'ellipsis',whiteSpace:'nowrap'}}>{API_URL.replace('/api','')}</div>
      </div>
    </div>
  );
}
