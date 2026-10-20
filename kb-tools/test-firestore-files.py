# Tests Firestore image storage rules exactly like the app does (anonymous user).
# Never prints the API key. Cleans up test documents and temporary accounts.
import json, base64, urllib.request, urllib.error
g=json.load(open('/home/user/Karbama/app/google-services.json'))
KEY=g['client'][0]['api_key'][0]['current_key']; PID=g['project_info']['project_id']
PKG=g['client'][0]['client_info']['android_client_info']['package_name']
FS=f'https://firestore.googleapis.com/v1/projects/{PID}/databases/(default)/documents'
def req(url, body=None, tok=None, method=None):
    h={'X-Android-Package':PKG,'Content-Type':'application/json'}
    if tok: h['Authorization']='Bearer '+tok
    r=urllib.request.Request(url, data=json.dumps(body).encode() if body is not None else None, headers=h, method=method)
    try:
        with urllib.request.urlopen(r, timeout=30) as resp: return resp.status, json.loads(resp.read() or b'{}')
    except urllib.error.HTTPError as e:
        try: return e.code, json.loads(e.read())
        except Exception: return e.code, {}
def anon():
    st,d=req(f'https://identitytoolkit.googleapis.com/v1/accounts:signUp?key={KEY}',{'returnSecureToken':True})
    assert st==200,('anon sign-in failed',st); return d['idToken'], d['localId']
def doc(uid,size,kind='id_card',ctype='image/jpeg'):
    data=base64.b64encode(b'\xff\xd8'+b'\x00'*(size-2)).decode()
    return {'fields':{'owner':{'stringValue':uid},'contentType':{'stringValue':ctype},'size':{'integerValue':str(size)},
            'data':{'bytesValue':data},'updatedAt':{'integerValue':'1'}}}
def status(st,d): return 'OK' if st==200 else f"{st} {d.get('error',{}).get('status','')}"
t1,u1=anon(); t2,u2=anon(); res={}
p1=f'{FS}/applicantFiles/{u1}/files/id_card'
st,d=req(p1,doc(u1,300_000),t1,'PATCH'); res['owner write 300KB (should be OK)']=status(st,d)
st,d=req(p1,None,t1); ok=st==200 and len(base64.b64decode(d['fields']['data']['bytesValue']))==300_000
res['owner read back (should be OK)']='OK' if ok else status(st,d)
st,d=req(p1,None,t2); res['other user read (should be 403)']=status(st,d)
st,d=req(f'{FS}/applicantFiles/{u1}/files/id_card',doc(u1,1000),t2,'PATCH'); res['other user overwrite (should be 403)']=status(st,d)
st,d=req(f'{FS}/applicantFiles/{u1}/files/random',doc(u1,1000),t1,'PATCH'); res['unknown kind (should be 403)']=status(st,d)
st,d=req(f'{FS}/applicantFiles/{u1}/files/body_photo',doc(u1,980_000),t1,'PATCH'); res['980KB image (should be 403)']=status(st,d)
st,d=req(f'{FS}/applicantFiles/{u1}/files/body_photo',doc(u1,1000,ctype='text/html'),t1,'PATCH'); res['non-JPEG (should be 403)']=status(st,d)
st,d=req(f'{FS}/jobFiles/test_{u1}',doc(u1,1000),t1,'PATCH'); res['non-admin job image (should be 403)']=status(st,d)
for k,v in res.items(): print(f'{v:28s} ← {k}')
# cleanup
for kind in ['id_card','body_photo','random']: req(f'{FS}/applicantFiles/{u1}/files/{kind}',None,t1,'DELETE')
for t in (t1,t2): req(f'https://identitytoolkit.googleapis.com/v1/accounts:delete?key={KEY}',{'idToken':t})
print('cleanup done (test docs + 2 temporary accounts deleted)')
