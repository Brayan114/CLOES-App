import React from 'react';
import { C } from '../utils';
import { Card, PH } from './Shared';

export default function Bloom() {
  return (
    <div style={{padding:28}} className="fade">
      <PH title="Bloom" sub="Connection health scores across the network"/>
      <Card style={{textAlign:'center',padding:50}}>
        <div style={{fontSize:52,marginBottom:16}}>🌸</div>
        <div style={{fontSize:18,fontWeight:700,marginBottom:10}}>Bloom Analytics</div>
        <div style={{color:C.sub,maxWidth:480,margin:'0 auto',lineHeight:1.8,fontSize:14}}>
          Bloom scores are calculated per conversation pair — based on message frequency, call duration, media shared, and streak days. Data populates as users interact.
        </div>
        <div style={{display:'flex',gap:14,justifyContent:'center',marginTop:30,flexWrap:'wrap'}}>
          {[[C.sub,'Low','Healthy baseline'],[C.gold,'Mid','Some drift detected'],[C.red,'High','Needs attention']].map(([c,l,d])=>(
            <div key={l} style={{background:C.s2,border:`1px solid ${C.border}`,borderRadius:12,padding:'16px 20px',textAlign:'left',minWidth:150}}>
              <div style={{width:10,height:10,borderRadius:'50%',background:c,marginBottom:10,boxShadow:`0 0 8px ${c}`}}/>
              <div style={{fontSize:13,fontWeight:700,marginBottom:4}}>{l} Urgency</div>
              <div style={{fontSize:11,color:C.sub,lineHeight:1.6}}>{d}</div>
            </div>
          ))}
        </div>
      </Card>
    </div>
  );
}
