import React, { useState, useEffect } from 'react';
import { PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { C, PAL, req, fmt, tstr } from '../utils';
import { Loader, Card, ST, PH, StatCard, TH, TD, Badge } from './Shared';

export default function Coins() {
  const [d,setD]       = useState(null);
  const [loading,setL] = useState(true);

  useEffect(()=>{
    req('GET','/admin/coins?page=1&limit=40').then(r=>{setD(r);setL(false)}).catch(()=>setL(false));
  },[]);

  if (loading) return <Loader/>;
  if (!d) return <div style={{padding:28,color:C.red}}>Failed to load coin data</div>;

  const earned = d.summary.find(s=>s._id==='earn')?.total||0;
  const spent  = Math.abs(d.summary.find(s=>s._id==='spend')?.total||0);

  return (
    <div style={{padding:28}} className="fade">
      <PH title="Coins" sub="Platform economy overview"/>
      <div style={{display:'grid',gridTemplateColumns:'repeat(3,1fr)',gap:14,marginBottom:22}}>
        <StatCard label="Total Earned"     value={fmt(earned)}       sub="All time"    color={C.gold}  icon="⬆"/>
        <StatCard label="Total Spent"      value={fmt(spent)}        sub="All time"    color={C.red}   icon="⬇"/>
        <StatCard label="Net Circulating"  value={fmt(earned-spent)} sub="In wallets"  color={C.green} icon="💰"/>
      </div>

      <div style={{display:'grid',gridTemplateColumns:'1fr 1fr',gap:18,marginBottom:22}}>
        <Card>
          <ST>Top Coin Earners</ST>
          {d.topEarners.length===0 ? <div style={{color:C.sub,fontSize:13}}>No data yet</div>
          : d.topEarners.map((u,i)=>(
            <div key={u._id} style={{display:'flex',alignItems:'center',gap:12,padding:'10px 0',borderBottom:i<d.topEarners.length-1?`1px solid ${C.border}30`:'none'}}>
              <div style={{fontSize:16,fontWeight:800,color:C.sub,fontFamily:"'Space Mono',monospace",width:24}}>#{i+1}</div>
              <div style={{width:32,height:32,borderRadius:8,background:`linear-gradient(135deg,${C.purple},${C.pink})`,display:'flex',alignItems:'center',justifyContent:'center',fontSize:13,fontWeight:700}}>{u.name?.[0]}</div>
              <div style={{flex:1,minWidth:0}}>
                <div style={{fontSize:13,fontWeight:600,overflow:'hidden',textOverflow:'ellipsis',whiteSpace:'nowrap'}}>{u.name}</div>
                <div style={{fontSize:11,color:C.sub}}>@{u.handle}</div>
              </div>
              <div style={{fontFamily:"'Space Mono',monospace",color:C.gold,fontWeight:700}}>{fmt(u.totalCoinsEarned||0)}</div>
            </div>
          ))}
        </Card>
        <Card>
          <ST>Transaction Breakdown</ST>
          {d.summary.length===0 ? <div style={{color:C.sub,fontSize:13,marginTop:8}}>No transactions yet</div>
          : <ResponsiveContainer width="100%" height={200}>
              <PieChart>
                <Pie data={d.summary.map(s=>({name:s._id,value:Math.abs(s.total)}))} cx="50%" cy="50%" outerRadius={70} paddingAngle={4} dataKey="value">
                  {d.summary.map((_,i)=><Cell key={i} fill={PAL[i]}/>)}
                </Pie>
                <Tooltip contentStyle={{background:C.s2,border:`1px solid ${C.border}`,borderRadius:8,fontSize:12}}/>
                <Legend wrapperStyle={{fontSize:12,color:C.mid}}/>
              </PieChart>
            </ResponsiveContainer>
          }
        </Card>
      </div>

      <Card style={{padding:0,overflow:'hidden'}}>
        <div style={{padding:'14px 20px',borderBottom:`1px solid ${C.border}`}}><ST>Recent Transactions</ST></div>
        <table style={{width:'100%',borderCollapse:'collapse'}}>
          <thead><tr><TH>User</TH><TH>Amount</TH><TH>Reason</TH><TH>Type</TH><TH>Balance After</TH><TH>Time</TH></tr></thead>
          <tbody>
            {d.transactions.length===0 ? <tr><td colSpan={6} style={{textAlign:'center',padding:40,color:C.sub,fontSize:13}}>No transactions yet</td></tr>
            : d.transactions.map(tx=>(
              <tr key={tx._id}>
                <TD style={{fontFamily:"'Space Mono',monospace",color:C.purple,fontSize:12}}>@{tx.user?.handle||'?'}</TD>
                <TD style={{fontFamily:"'Space Mono',monospace",color:tx.amount>=0?C.green:C.red,fontWeight:700}}>{tx.amount>=0?'+':''}{tx.amount}</TD>
                <TD style={{fontSize:12}}>{tx.reason}</TD>
                <TD><Badge text={tx.type} c={tx.type==='earn'?C.green:tx.type==='spend'?C.red:C.gold}/></TD>
                <TD style={{fontFamily:"'Space Mono',monospace",color:C.gold,fontWeight:700}}>{tx.balance}</TD>
                <TD style={{fontSize:11,color:C.sub,whiteSpace:'nowrap'}}>{tstr(tx.createdAt)}</TD>
              </tr>
            ))}
          </tbody>
        </table>
      </Card>
    </div>
  );
}
