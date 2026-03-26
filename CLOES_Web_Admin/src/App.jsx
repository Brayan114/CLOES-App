import React, { useState, useEffect } from 'react';
import { TOKEN, req, setToken } from './utils';

// Components
import Login from './components/Login';
import Sidebar from './components/Sidebar';
import Topbar from './components/Topbar';
import Dashboard from './components/Dashboard';
import Users from './components/Users';
import Messages from './components/Messages';
import Vibes from './components/Vibes';
import Coins from './components/Coins';
import Bloom from './components/Bloom';
import Settings from './components/Settings';

export default function App() {
  const [admin,setAdmin]     = useState(null);
  const [page,setPage]       = useState('dashboard');
  const [checking,setCheck]  = useState(true);

  useEffect(() => {
    if (TOKEN) {
      req('GET','/auth/me')
        .then(d => { 
          if(d.user?.isAdmin){
            setAdmin(d.user);
          } else {
            setToken('');
          } 
        })
        .catch(() => { setToken(''); })
        .finally(() => setCheck(false));
    } else {
      setCheck(false);
    }
  }, []);

  const logout = () => { 
    setToken(''); 
    setAdmin(null); 
  };

  if (checking) return null;
  if (!admin) return <Login onLogin={setAdmin}/>;

  return (
    <div style={{display:'flex',minHeight:'100vh'}}>
      <Sidebar page={page} setPage={setPage} admin={admin} logout={logout}/>
      <div style={{flex:1,marginLeft:226,display:'flex',flexDirection:'column'}}>
        <Topbar/>
        <main style={{flex:1}}>
          {page==='dashboard' && <Dashboard/>}
          {page==='users'     && <Users/>}
          {page==='messages'  && <Messages/>}
          {page==='vibes'     && <Vibes/>}
          {page==='coins'     && <Coins/>}
          {page==='bloom'     && <Bloom/>}
          {page==='settings'  && <Settings admin={admin}/>}
        </main>
      </div>
    </div>
  );
}
