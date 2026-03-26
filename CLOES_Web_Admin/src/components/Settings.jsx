import React, { useState } from 'react';
import { C, API_URL, setApiUrl } from '../utils';
import { Card, ST, PH, Inp, Lbl, Btn, Badge } from './Shared';

export default function Settings({admin}) {
  const [url,setUrl]   = useState(API_URL);
  const [saved,setSaved] = useState(false);

  const save = () => { setApiUrl(url); setSaved(true); setTimeout(()=>setSaved(false),2000); };

  return (
    <div style={{padding:28}} className="fade">
      <PH title="Settings" sub="Portal configuration"/>
      <div style={{maxWidth:540,display:'flex',flexDirection:'column',gap:18}}>

        <Card>
          <ST>Admin Account</ST>
          <div style={{display:'flex',alignItems:'center',gap:14}}>
            <div style={{width:48,height:48,borderRadius:14,background:`linear-gradient(135deg,${C.purple},${C.pink})`,display:'flex',alignItems:'center',justifyContent:'center',fontSize:20,fontWeight:700,flexShrink:0}}>{admin?.name?.[0]||'A'}</div>
            <div>
              <div style={{fontSize:16,fontWeight:700}}>{admin?.name}</div>
              <div style={{fontSize:12,color:C.sub,marginTop:2}}>@{admin?.handle} · {admin?.email}</div>
              <Badge text="SUPER ADMIN" c={C.gold}/>
            </div>
          </div>
        </Card>

        <Card>
          <ST>API Connection</ST>
          <div style={{display:'flex',flexDirection:'column',gap:12}}>
            <div><Lbl>Backend URL</Lbl><Inp value={url} onChange={e=>setUrl(e.target.value)} placeholder="http://localhost:5000/api"/></div>
            <div style={{fontSize:11,color:C.sub}}>Change this if your backend runs on a different port or domain</div>
            <Btn onClick={save} style={{padding:'10px 20px',alignSelf:'flex-start',background:`linear-gradient(135deg,${C.purple},${C.pink})`}}>
              {saved?'✓ Saved':'Save & Reconnect ✦'}
            </Btn>
          </div>
        </Card>

        <Card>
          <ST>Stack Info</ST>
          <div style={{display:'flex',flexDirection:'column',gap:9}}>
            {[['Portal','React built with Vite (originally standalone HTML)'],['Backend','Node.js + Express + Socket.io'],['Database','MongoDB Atlas'],['Media','Cloudinary'],['Push','Firebase Admin SDK'],['Auth','JWT + Google OAuth 2.0']].map(([k,v])=>(
              <div key={k} style={{display:'flex',gap:14}}>
                <div style={{fontSize:12,color:C.sub,width:80,flexShrink:0}}>{k}</div>
                <div style={{fontSize:11,color:C.mid,fontFamily:"'Space Mono',monospace"}}>{v}</div>
              </div>
            ))}
          </div>
        </Card>
      </div>
    </div>
  );
}
