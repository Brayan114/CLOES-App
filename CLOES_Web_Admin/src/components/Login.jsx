import React, { useState } from 'react';
import { C, API_URL, setApiUrl, req, setToken } from '../utils';
import { Spin, Inp, Lbl, Btn } from './Shared';

export default function Login({onLogin}) {
  const [email,setEmail]   = useState('');
  const [pass,setPass]     = useState('');
  const [apiUrlLocal,setApiUrlLocal] = useState(API_URL);
  const [showApi,setShowApi] = useState(false);
  const [err,setErr]       = useState('');
  const [loading,setLoad]  = useState(false);

  const go = async e => {
    e.preventDefault(); setLoad(true); setErr('');
    setApiUrl(apiUrlLocal);
    try {
      const d = await req('POST','/auth/login',{email,password:pass});
      if (!d.user?.isAdmin) { setErr('Not an admin account.\nFix: node make-admin.js '+email); setLoad(false); return; }
      setToken(d.token);
      onLogin(d.user);
    } catch(e){ setErr(e.message); }
    setLoad(false);
  };

  return (
    <div style={{minHeight:'100vh',background:C.bg,display:'flex',alignItems:'center',justifyContent:'center',position:'relative'}}>
      <div style={{position:'fixed',top:'5%',left:'20%',width:600,height:600,background:`${C.purple}10`,borderRadius:'50%',filter:'blur(140px)',pointerEvents:'none'}}/>
      <div style={{position:'fixed',bottom:'5%',right:'10%',width:400,height:400,background:`${C.pink}0C`,borderRadius:'50%',filter:'blur(110px)',pointerEvents:'none'}}/>

      <div className="fade" style={{background:C.surface,border:`1px solid ${C.border}`,borderRadius:20,padding:36,width:400,maxWidth:'92vw',zIndex:1,position:'relative'}}>
        <div style={{textAlign:'center',marginBottom:32}}>
          <div style={{fontSize:38,fontWeight:800,background:`linear-gradient(135deg,${C.purple},${C.pink})`,WebkitBackgroundClip:'text',WebkitTextFillColor:'transparent',letterSpacing:8,fontFamily:"'Space Mono',monospace"}}>CLOES</div>
          <div style={{color:C.sub,fontSize:11,letterSpacing:'0.18em',marginTop:4}}>ADMIN PORTAL</div>
        </div>

        <form onSubmit={go} style={{display:'flex',flexDirection:'column',gap:16}}>
          <div>
            <div style={{display:'flex',justifyContent:'space-between',alignItems:'center',marginBottom:6}}>
              <Lbl>Server URL</Lbl>
              <button type="button" onClick={()=>setShowApi(!showApi)} style={{background:'none',border:'none',color:C.purple,fontSize:11,cursor:'pointer',textDecoration:'underline'}}>{showApi?'hide':'change'}</button>
            </div>
            {showApi
              ? <Inp value={apiUrlLocal} onChange={e=>setApiUrlLocal(e.target.value)} placeholder="http://localhost:5000/api"/>
              : <div style={{fontSize:11,color:C.sub,background:C.s2,borderRadius:8,padding:'8px 12px',fontFamily:"'Space Mono',monospace",overflow:'hidden',textOverflow:'ellipsis',whiteSpace:'nowrap'}}>{apiUrlLocal}</div>
            }
          </div>
          <div><Lbl>Admin Email</Lbl><Inp type="email" value={email} onChange={e=>setEmail(e.target.value)} placeholder="admin@cloes.app" required/></div>
          <div><Lbl>Password</Lbl><Inp type="password" value={pass} onChange={e=>setPass(e.target.value)} placeholder="••••••••" required/></div>
          {err && <div style={{background:`${C.red}15`,border:`1px solid ${C.red}40`,borderRadius:10,padding:'12px 14px',color:C.red,fontSize:12,lineHeight:1.7,whiteSpace:'pre-wrap'}}>{err}</div>}
          <Btn disabled={loading} style={{padding:13,background:`linear-gradient(135deg,${C.purple},${C.pink})`,fontSize:14}}>
            {loading ? <span style={{display:'flex',alignItems:'center',justifyContent:'center',gap:8}}><Spin s={16}/> Signing in…</span> : 'Sign In ✦'}
          </Btn>
        </form>
      </div>
    </div>
  );
}
