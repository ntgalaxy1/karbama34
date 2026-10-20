# v50: live test of the uniqueKeys rules (duplicate national ID / phone) — exactly like the app.
exec(open('/home/user/kb-tools/test-firestore-files.py').read().split('t1,u1=anon()')[0])
import hashlib, random
def key(kind, val): return ('nid_' if kind=='nid' else 'ph_') + hashlib.sha256(f'karbama|{"nid" if kind=="nid" else "ph"}|{val}'.encode()).hexdigest()
def body(owner, kind): return {'fields':{'owner':{'stringValue':owner},'kind':{'stringValue':kind},'createdAt':{'timestampValue':'2026-10-20T00:00:00Z'}}}
res=[]
def chk(name, st, d, expect):
    ok = (st==200)==expect; res.append(ok); print(('✅' if ok else '❌'), f'{name:52s}', status(st,d))
A,ua=anon(); Bt,ub=anon()
nid=f'99{random.randint(10**7,10**8-1)}'; kn=key('nid',nid); kp=key('phone','90599'+str(random.randint(10**6,10**7-1)))
st,d=req(f'{FS}/uniqueKeys/{kn}',None,Bt); chk('B checks a free national ID (not found = 404)', 200 if st==404 else st, d, True)
st,d=req(f'{FS}/uniqueKeys/{kn}',body(ua,'nid'),A,'PATCH'); chk('A claims own national ID',st,d,True)
st,d=req(f'{FS}/uniqueKeys/{kp}',body(ua,'phone'),A,'PATCH'); chk('A claims own phone',st,d,True)
st,d=req(f'{FS}/uniqueKeys/{kn}',None,Bt); chk('B checks same national ID → exists (taken)',st,d,True)
print('   owner returned to B:', 'A' if st==200 and d['fields']['owner']['stringValue']==ua else '?')
st,d=req(f'{FS}/uniqueKeys/{kn}',body(ub,'nid'),Bt,'PATCH'); chk('SECURITY: B takes over A\'s national ID → deny',st,d,False)
st,d=req(f'{FS}/uniqueKeys/{kp}',None,Bt,'DELETE'); chk('SECURITY: B deletes A\'s phone key → deny',st,d,False)
st,d=req(f'{FS}/uniqueKeys',None,Bt); chk('SECURITY: B lists all keys → deny',st,d,False)
st,d=req(f'{FS}/uniqueKeys/nid_123',body(ub,'nid'),Bt,'PATCH'); chk('SECURITY: malformed key name → deny',st,d,False)
st,d=req(f'{FS}/uniqueKeys/{key("nid","1111111111")}',body(ua,'nid'),Bt,'PATCH'); chk('SECURITY: B creates key in A\'s name → deny',st,d,False)
st,d=req(f'{FS}/uniqueKeys/{kp}',None,A,'DELETE'); chk('A releases own phone key (phone change)',st,d,True)
st,d=req(f'{FS}/uniqueKeys/{kn}',None,A,'DELETE'); res.append(st==200)
for t in (A,Bt): req(f'https://identitytoolkit.googleapis.com/v1/accounts:delete?key={KEY}',{'idToken':t})
print('RESULT:', 'PASS' if all(res) else 'FAIL', f'({sum(res)}/{len(res)}) — test keys + accounts cleaned up')
