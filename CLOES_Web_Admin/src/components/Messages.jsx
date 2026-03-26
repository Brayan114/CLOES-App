import React, { useState, useEffect, useCallback } from 'react';
import { C, req, tstr } from '../utils';
import { Spin, Inp, Card, PH, Pager, Toast, useToast, TH, TD, Badge } from './Shared';

export default function Messages() {
  const [rows,setRows]   = useState([]);
  const [total,setTotal] = useState(0);
  const [pg,setPg]       = useState(1);
  const [q,setQ]         = useState('');
  const [loading,setL]   = useState(false);
  const [toast,showToast] = useToast();

  const load = useCallback(async()=>{
    setL(true);
    const p = new URLSearchParams({page:pg,limit:30,search:q});
    const d = await req('GET',`/admin/messages?${p}`).catch(()=>({messages:[],total:0}));
    setRows(d.messages||[]); setTotal(d.total||0); setL(false);
  },[pg,q]);

  useEffect(()=>{load();},[load]);

  const del = async id => { if(!confirm('Delete permanently?'))return; await req('DELETE',`/admin/messages/${id}`).catch(e=>showToast(e.message)); showToast('Deleted'); load(); };

  const TC = {text:C.sub,image:C.cyan,video:C.pink,audio:C.mint,document:C.gold,link:C.purple,deleted:C.red};

  return (
    <div style={{padding:28}} className="fade">
      <Toast msg={toast}/>
      <PH title="Messages" sub={`${total.toLocaleString()} total messages`}/>
      <Inp style={{width:320,marginBottom:16}} placeholder="🔍  Search content…" value={q} onChange={e=>{setQ(e.target.value);setPg(1)}}/>
      <Card style={{padding:0,overflow:'hidden'}}>
        <table style={{width:'100%',borderCollapse:'collapse'}}>
          <thead><tr><TH>Sender</TH><TH>Content</TH><TH>Type</TH><TH>Sent</TH><TH>Delete</TH></tr></thead>
          <tbody>
            {loading ? <tr><td colSpan={5} style={{textAlign:'center',padding:50}}><Spin s={32}/></td></tr>
            : rows.length===0 ? <tr><td colSpan={5} style={{textAlign:'center',padding:40,color:C.sub,fontSize:13}}>No messages found</td></tr>
            : rows.map(m=>(
              <tr key={m._id}>
                <TD style={{fontFamily:"'Space Mono',monospace",color:C.purple,fontSize:12,whiteSpace:'nowrap'}}>@{m.sender?.handle||'?'}</TD>
                <TD style={{maxWidth:340}}>
                  <div style={{overflow:'hidden',textOverflow:'ellipsis',whiteSpace:'nowrap',color:C.text}}>{m.text||<em style={{color:C.sub}}>[{m.type}]</em>}</div>
                  {m.mediaUrl&&<div style={{fontSize:11,color:C.cyan,marginTop:2,overflow:'hidden',textOverflow:'ellipsis',whiteSpace:'nowrap'}}>🔗 {m.mediaUrl}</div>}
                </TD>
                <TD><Badge text={m.type} c={TC[m.type]||C.sub}/></TD>
                <TD style={{fontSize:11,color:C.sub,whiteSpace:'nowrap'}}>{tstr(m.createdAt)}</TD>
                <TD><button onClick={()=>del(m._id)} style={{background:`${C.red}20`,border:'none',borderRadius:7,padding:'5px 10px',cursor:'pointer',color:C.red,fontSize:12,fontWeight:600}}>Delete</button></TD>
              </tr>
            ))}
          </tbody>
        </table>
      </Card>
      <Pager page={pg} total={total} limit={30} go={setPg}/>
    </div>
  );
}
