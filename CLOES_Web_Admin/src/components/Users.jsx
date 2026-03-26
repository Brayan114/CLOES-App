import React, { useState, useEffect, useCallback } from 'react';
import { C, req, dstr } from '../utils';
import { Spin, Inp, Lbl, Btn, Card, PH, Modal, Pager, Toast, useToast, TH, TD, Badge } from './Shared';

export default function Users() {
  const [rows,setRows]     = useState([]);
  const [total,setTotal]   = useState(0);
  const [pg,setPg]         = useState(1);
  const [q,setQ]           = useState('');
  const [fl,setFl]         = useState('');
  const [loading,setL]     = useState(false);
  const [modal,setModal]   = useState(null);
  const [amt,setAmt]       = useState('');
  const [rsn,setRsn]       = useState('');
  const [toast,showToast]  = useToast();

  const load = useCallback(async()=>{
    setL(true);
    const p = new URLSearchParams({page:pg,limit:20,search:q,filter:fl});
    const d = await req('GET',`/admin/users?${p}`).catch(()=>({users:[],total:0}));
    setRows(d.users||[]); setTotal(d.total||0); setL(false);
  },[pg,q,fl]);

  useEffect(()=>{load();},[load]);

  const ban   = async(id,banned) => { const r=banned?(prompt('Ban reason:')||''):''; if(r===null)return; await req('POST',`/admin/users/${id}/ban`,{banned,reason:r}).catch(e=>showToast(e.message)); showToast(banned?'User banned':'User unbanned'); load(); };
  const admin = async(id,v)      => { await req('PATCH',`/admin/users/${id}`,{isAdmin:v}).catch(e=>showToast(e.message)); showToast(v?'Promoted to admin':'Admin removed'); load(); };
  const del   = async id         => { if(!confirm('Delete this user permanently?'))return; await req('DELETE',`/admin/users/${id}`).catch(e=>showToast(e.message)); showToast('User deleted'); load(); };
  const coins = async()          => { if(!amt)return; await req('POST',`/admin/users/${modal._id}/coins`,{amount:Number(amt),reason:rsn||'Admin grant'}).catch(e=>showToast(e.message)); showToast(`Coins updated`); setModal(null);setAmt('');setRsn(''); load(); };

  const FLS = [['','All'],['online','Online'],['google','Google'],['local','Local'],['admin','Admins']];

  return (
    <div style={{padding:28}} className="fade">
      <Toast msg={toast}/>
      <PH title="Users" sub={`${total.toLocaleString()} total users`}/>

      <div style={{display:'flex',gap:12,marginBottom:16,flexWrap:'wrap',alignItems:'center'}}>
        <Inp style={{width:260}} placeholder="🔍  Search name, handle, email…" value={q} onChange={e=>{setQ(e.target.value);setPg(1)}}/>
        <div style={{display:'flex',gap:6,flexWrap:'wrap'}}>
          {FLS.map(([f,l])=>(
            <button key={f} onClick={()=>{setFl(f);setPg(1)}} style={{
              background:fl===f?`${C.purple}20`:'transparent',border:`1px solid ${fl===f?C.purple:C.border}`,
              borderRadius:9,padding:'7px 13px',color:fl===f?C.purple:C.sub,cursor:'pointer',fontSize:12,fontFamily:'inherit',fontWeight:fl===f?600:400
            }}>{l}</button>
          ))}
        </div>
      </div>

      <Card style={{padding:0,overflow:'hidden'}}>
        <table style={{width:'100%',borderCollapse:'collapse'}}>
          <thead><tr>
            <TH>User</TH><TH>Handle</TH><TH>Auth</TH><TH>Coins</TH><TH>Status</TH><TH>Joined</TH><TH>Actions</TH>
          </tr></thead>
          <tbody>
            {loading ? <tr><td colSpan={7} style={{textAlign:'center',padding:50}}><Spin s={32}/></td></tr>
            : rows.length===0 ? <tr><td colSpan={7} style={{textAlign:'center',padding:40,color:C.sub,fontSize:13}}>No users found</td></tr>
            : rows.map(u => (
              <tr key={u._id}>
                <TD>
                  <div style={{display:'flex',alignItems:'center',gap:10}}>
                    <div style={{width:34,height:34,borderRadius:9,background:`linear-gradient(135deg,${C.purple},${C.pink})`,display:'flex',alignItems:'center',justifyContent:'center',fontSize:13,fontWeight:700,overflow:'hidden',flexShrink:0}}>
                      {u.avatar?<img src={u.avatar} alt="" style={{width:'100%',height:'100%',objectFit:'cover'}}/>:u.name?.[0]}
                    </div>
                    <div>
                      <div style={{fontSize:13,fontWeight:600,color:C.text,whiteSpace:'nowrap'}}>{u.name}</div>
                      <div style={{display:'flex',gap:4,marginTop:2}}>
                        {u.isAdmin && <Badge text="ADMIN" c={C.gold}/>}
                        {u.banned  && <Badge text="BANNED" c={C.red}/>}
                      </div>
                    </div>
                  </div>
                </TD>
                <TD style={{fontFamily:"'Space Mono',monospace",color:C.purple,fontSize:12}}>@{u.handle}</TD>
                <TD><Badge text={u.authProvider} c={u.authProvider==='google'?C.cyan:C.mint}/></TD>
                <TD style={{fontFamily:"'Space Mono',monospace",color:C.gold,fontWeight:700}}>{u.coinBalance||0}</TD>
                <TD><Badge text={u.online?'Online':'Offline'} c={u.online?C.green:C.sub}/></TD>
                <TD style={{fontSize:11,color:C.sub,whiteSpace:'nowrap'}}>{dstr(u.createdAt)}</TD>
                <TD>
                  <div style={{display:'flex',gap:5}}>
                    <button title="Manage coins" onClick={()=>setModal(u)} style={{background:`${C.gold}20`,border:'none',borderRadius:7,padding:'5px 8px',cursor:'pointer',fontSize:13}}>🪙</button>
                    <button title={u.banned?'Unban':'Ban'} onClick={()=>ban(u._id,!u.banned)} style={{background:`${u.banned?C.green:C.red}20`,border:'none',borderRadius:7,padding:'5px 8px',cursor:'pointer',fontSize:13}}>{u.banned?'✓':'🚫'}</button>
                    <button title={u.isAdmin?'Remove admin':'Make admin'} onClick={()=>admin(u._id,!u.isAdmin)} style={{background:`${C.purple}20`,border:'none',borderRadius:7,padding:'5px 8px',cursor:'pointer',fontSize:13}}>{u.isAdmin?'⬇':'⭐'}</button>
                    <button title="Delete" onClick={()=>del(u._id)} style={{background:`${C.red}20`,border:'none',borderRadius:7,padding:'5px 8px',cursor:'pointer',fontSize:13}}>✕</button>
                  </div>
                </TD>
              </tr>
            ))}
          </tbody>
        </table>
      </Card>
      <Pager page={pg} total={total} limit={20} go={setPg}/>

      {modal && (
        <Modal title={`Coins — ${modal.name}`} onClose={()=>setModal(null)}>
          <div style={{display:'flex',flexDirection:'column',gap:14}}>
            <div style={{background:C.s2,borderRadius:10,padding:'10px 14px',fontSize:12,color:C.sub}}>Current balance: <strong style={{color:C.gold}}>{modal.coinBalance||0}</strong></div>
            <div><Lbl>Amount (+ grant / − deduct)</Lbl><Inp type="number" value={amt} onChange={e=>setAmt(e.target.value)} placeholder="e.g. 100"/></div>
            <div><Lbl>Reason</Lbl><Inp value={rsn} onChange={e=>setRsn(e.target.value)} placeholder="Admin grant"/></div>
            <Btn onClick={coins} style={{padding:12,background:`linear-gradient(135deg,${C.gold},${C.pink})`}}>Apply ✦</Btn>
          </div>
        </Modal>
      )}
    </div>
  );
}
