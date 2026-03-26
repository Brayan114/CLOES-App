import React, { useState, useEffect, useCallback } from 'react';
import { C, req, fmt } from '../utils';
import { Loader, Inp, Card, PH, Pager, Toast, useToast, Badge } from './Shared';

export default function Vibes() {
  const [rows,setRows]   = useState([]);
  const [total,setTotal] = useState(0);
  const [pg,setPg]       = useState(1);
  const [q,setQ]         = useState('');
  const [loading,setL]   = useState(false);
  const [toast,showToast] = useToast();

  const load = useCallback(async()=>{
    setL(true);
    const p = new URLSearchParams({page:pg,limit:18,search:q});
    const d = await req('GET',`/admin/vibes?${p}`).catch(()=>({vibes:[],total:0}));
    setRows(d.vibes||[]); setTotal(d.total||0); setL(false);
  },[pg,q]);

  useEffect(()=>{load();},[load]);

  const del    = async id   => { if(!confirm('Delete permanently?'))return; await req('DELETE',`/admin/vibes/${id}`).catch(e=>showToast(e.message)); showToast('Deleted'); load(); };
  const status = async(id,s)=> { await req('PATCH',`/admin/vibes/${id}/status`,{status:s}).catch(e=>showToast(e.message)); showToast(`Status → ${s}`); load(); };
  const SC = {ready:C.green,processing:C.gold,failed:C.red};

  return (
    <div style={{padding:28}} className="fade">
      <Toast msg={toast}/>
      <PH title="Vibe Videos" sub={`${total.toLocaleString()} total vibes`}/>
      <Inp style={{width:320,marginBottom:18}} placeholder="🔍  Search title…" value={q} onChange={e=>{setQ(e.target.value);setPg(1)}}/>

      {loading ? <Loader/> : rows.length===0 ? (
        <div style={{textAlign:'center',padding:60,color:C.sub}}>No vibes found</div>
      ) : (
        <div style={{display:'grid',gridTemplateColumns:'repeat(auto-fill,minmax(230px,1fr))',gap:14,marginBottom:20}}>
          {rows.map(v=>{
            const c1=v.paletteColors?.[0]||C.purple, c2=v.paletteColors?.[1]||C.pink;
            return (
              <Card key={v._id} style={{padding:0,overflow:'hidden'}}>
                <div style={{height:100,background:v.thumbnailUrl?`url(${v.thumbnailUrl}) center/cover`:`linear-gradient(135deg,${c1},${c2})`,position:'relative'}}>
                  <div style={{position:'absolute',top:8,right:8}}><Badge text={v.status} c={SC[v.status]||C.sub}/></div>
                  <div style={{position:'absolute',bottom:8,left:8}}><Badge text={v.category||'?'} c={C.pink}/></div>
                </div>
                <div style={{padding:12}}>
                  <div style={{fontSize:13,fontWeight:600,color:C.text,overflow:'hidden',textOverflow:'ellipsis',whiteSpace:'nowrap',marginBottom:3}}>{v.title}</div>
                  <div style={{fontSize:11,color:C.sub,marginBottom:10}}>@{v.creator?.handle||'?'} · {fmt(v.views||0)} views</div>
                  <div style={{display:'flex',gap:6,flexWrap:'wrap'}}>
                    {v.status!=='ready' && <button onClick={()=>status(v._id,'ready')} style={{background:`${C.green}20`,border:'none',borderRadius:7,padding:'4px 10px',cursor:'pointer',color:C.green,fontSize:12,fontWeight:600}}>Approve</button>}
                    {v.status==='ready' && <button onClick={()=>status(v._id,'failed')} style={{background:`${C.gold}20`,border:'none',borderRadius:7,padding:'4px 10px',cursor:'pointer',color:C.gold,fontSize:12,fontWeight:600}}>Hide</button>}
                    <button onClick={()=>del(v._id)} style={{background:`${C.red}20`,border:'none',borderRadius:7,padding:'4px 10px',cursor:'pointer',color:C.red,fontSize:12,fontWeight:600}}>Delete</button>
                  </div>
                </div>
              </Card>
            );
          })}
        </div>
      )}
      <Pager page={pg} total={total} limit={18} go={setPg}/>
    </div>
  );
}
