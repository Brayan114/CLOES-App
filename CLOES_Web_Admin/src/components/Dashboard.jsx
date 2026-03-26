import React, { useState, useEffect } from 'react';
import { LineChart, Line, BarChart, Bar, PieChart, Pie, Cell, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { C, PAL, req, fmt, week, API_URL } from '../utils';
import { Loader, Card, ST, PH, StatCard } from './Shared';

export default function Dashboard() {
  const [d,setD]         = useState(null);
  const [loading,setL]   = useState(true);
  const [err,setErr]     = useState('');

  useEffect(()=>{
    req('GET','/admin/stats').then(r=>{setD(r);setL(false)}).catch(e=>{setErr(e.message);setL(false)});
  },[]);

  if (loading) return <Loader/>;
  if (err) return (
    <div style={{padding:28}}>
      <PH title="Dashboard" sub=""/>
      <Card style={{borderColor:`${C.red}40`,background:`${C.red}10`}}>
        <div style={{color:C.red,lineHeight:1.9,fontSize:14}}>
          <strong>Cannot load stats:</strong> {err}<br/><br/>
          Make sure the backend is running at <code style={{background:C.s2,padding:'2px 6px',borderRadius:4,fontSize:12}}>{API_URL}</code><br/>
          and that it was restarted after admin routes were added (admin route must be in index.js).
        </div>
      </Card>
    </div>
  );
  if (!d) return null;
  const {totals:t,charts:ch} = d;

  return (
    <div style={{padding:28}} className="fade">
      <PH title="Dashboard" sub="Real-time overview of your CLOES universe"/>

      <div style={{display:'grid',gridTemplateColumns:'repeat(4,1fr)',gap:14,marginBottom:16}}>
        <StatCard label="Total Users"    value={fmt(t.users)}        sub={`+${t.newUsersToday} today`} color={C.purple} icon="◉"/>
        <StatCard label="Online Now"     value={t.onlineUsers}        sub="Live right now"               color={C.green}  icon="🟢"/>
        <StatCard label="Total Messages" value={fmt(t.messages)}      sub={`${t.messagesToday} today`}   color={C.cyan}   icon="💬"/>
        <StatCard label="Vibe Videos"    value={fmt(t.vibes)}         sub="Uploaded"                     color={C.pink}   icon="▶"/>
      </div>
      <div style={{display:'grid',gridTemplateColumns:'repeat(3,1fr)',gap:14,marginBottom:22}}>
        <StatCard label="Coins Awarded"  value={fmt(t.coins)}         sub="All time"      color={C.gold}   icon="🪙"/>
        <StatCard label="Conversations"  value={fmt(t.conversations)} sub="Total threads" color={C.mint}   icon="✦"/>
        <StatCard label="Messages Today" value={t.messagesToday}      sub="Last 24 hours" color={C.purple} icon="⚡"/>
      </div>

      <div style={{display:'grid',gridTemplateColumns:'1fr 1fr',gap:18,marginBottom:18}}>
        <Card>
          <ST>User Growth — Last 7 Days</ST>
          <ResponsiveContainer width="100%" height={180}>
            <BarChart data={week(ch.userGrowth)} margin={{top:5,right:5,bottom:5,left:-22}}>
              <CartesianGrid strokeDasharray="3 3" stroke={C.border}/>
              <XAxis dataKey="day" tick={{fill:C.sub,fontSize:11}}/>
              <YAxis tick={{fill:C.sub,fontSize:11}} allowDecimals={false}/>
              <Tooltip contentStyle={{background:C.s2,border:`1px solid ${C.border}`,borderRadius:8,color:C.text,fontSize:12}}/>
              <Bar dataKey="count" fill={C.purple} radius={[4,4,0,0]}/>
            </BarChart>
          </ResponsiveContainer>
        </Card>
        <Card>
          <ST>Message Activity — Last 7 Days</ST>
          <ResponsiveContainer width="100%" height={180}>
            <LineChart data={week(ch.msgActivity)} margin={{top:5,right:5,bottom:5,left:-22}}>
              <CartesianGrid strokeDasharray="3 3" stroke={C.border}/>
              <XAxis dataKey="day" tick={{fill:C.sub,fontSize:11}}/>
              <YAxis tick={{fill:C.sub,fontSize:11}} allowDecimals={false}/>
              <Tooltip contentStyle={{background:C.s2,border:`1px solid ${C.border}`,borderRadius:8,color:C.text,fontSize:12}}/>
              <Line type="monotone" dataKey="count" stroke={C.pink} strokeWidth={2.5} dot={{fill:C.pink,r:3}}/>
            </LineChart>
          </ResponsiveContainer>
        </Card>
      </div>

      <div style={{display:'grid',gridTemplateColumns:'1fr 1fr',gap:18}}>
        <Card>
          <ST>Auth Provider Breakdown</ST>
          <div style={{display:'flex',alignItems:'center',gap:16}}>
            <ResponsiveContainer width={150} height={150}>
              <PieChart>
                <Pie data={ch.authBreakdown.map(a=>({name:a._id||'local',value:a.count}))} cx="50%" cy="50%" innerRadius={36} outerRadius={62} paddingAngle={4} dataKey="value">
                  {ch.authBreakdown.map((_,i)=><Cell key={i} fill={PAL[i]}/>)}
                </Pie>
                <Tooltip contentStyle={{background:C.s2,border:`1px solid ${C.border}`,borderRadius:8,fontSize:12}}/>
              </PieChart>
            </ResponsiveContainer>
            <div style={{display:'flex',flexDirection:'column',gap:10}}>
              {ch.authBreakdown.map((a,i)=>(
                <div key={i} style={{display:'flex',alignItems:'center',gap:8}}>
                  <div style={{width:10,height:10,borderRadius:3,background:PAL[i%PAL.length],flexShrink:0}}/>
                  <span style={{fontSize:13,color:C.mid,textTransform:'capitalize'}}>{a._id||'local'}</span>
                  <span style={{fontSize:14,fontWeight:700,color:C.text,marginLeft:'auto',paddingLeft:8}}>{a.count}</span>
                </div>
              ))}
            </div>
          </div>
        </Card>
        <Card>
          <ST>Vibe Categories</ST>
          <div style={{display:'flex',flexDirection:'column',gap:10,marginTop:4}}>
            {(ch.vibeCategories?.length?ch.vibeCategories:[{_id:'No vibes yet',count:0}]).slice(0,6).map((c,i)=>(
              <div key={i} style={{display:'flex',alignItems:'center',gap:10}}>
                <div style={{fontSize:11,color:C.sub,width:66,textTransform:'uppercase',letterSpacing:'0.05em',flexShrink:0,overflow:'hidden',textOverflow:'ellipsis',whiteSpace:'nowrap'}}>{c._id}</div>
                <div style={{flex:1,height:5,background:C.s2,borderRadius:3,overflow:'hidden'}}>
                  <div style={{height:'100%',borderRadius:3,background:PAL[i%PAL.length],width:`${Math.min(100,(c.count/(ch.vibeCategories[0]?.count||1))*100)}%`}}/>
                </div>
                <div style={{fontSize:13,color:C.text,fontWeight:600,width:24,textAlign:'right'}}>{c.count}</div>
              </div>
            ))}
          </div>
        </Card>
      </div>
    </div>
  );
}
