# Full v49 flow exactly like the app, against the LIVE Firestore rules:
# anonymous user → 2 photos → applicants/{uid} (no paymentAt) → profile merge edit → manual receipt → security checks.
# Never prints the API key. Deletes test photos + temporary account (applicant record is admin-delete only).
exec(open('/home/user/kb-tools/test-firestore-files.py').read().split('t1,u1=anon()')[0])
import urllib.parse
def v(x):
    if isinstance(x,bool): return {'booleanValue':x}
    if isinstance(x,int): return {'integerValue':str(x)}
    if isinstance(x,str): return {'stringValue':x}
    if isinstance(x,list): return {'arrayValue':{'values':[v(i) for i in x]}} if x else {'arrayValue':{}}
    if isinstance(x,dict): return {'mapValue':{'fields':{k:v(i) for k,i in x.items()}}}
def patch(path, fields, tok, mask=None):
    q='' if mask is None else '?'+'&'.join('updateMask.fieldPaths='+urllib.parse.quote(m) for m in mask)
    return req(f'{FS}/{path}{q}',{'fields':{k:v(x) for k,x in fields.items()}},tok,'PATCH')
t,u=anon(); res=[]
def chk(name, st, d, expect_ok):
    ok=(st==200)==expect_ok; res.append(ok); print(('✅' if ok else '❌'), f'{name:46s}', status(st,d))
for kind in ['id_card','body_photo']:
    st,d=req(f'{FS}/applicantFiles/{u}/files/{kind}',doc(u,200_000,kind),t,'PATCH'); chk(f'photo {kind}',st,d,True)
app={'uid':u,'firstName':'TEST','lastName':'DELETE-ME','fatherName':'-','username':'zz_test_'+u[:6].lower(),'passwordHash':'','nationalId':'0000000000',
 'otherJobs':'','phone':'','email':'','city':'İstanbul','district':'','address':'','jobKeys':['cook'],'spokenLangs':['fa'],
 'trLevel':40,'gender':'m','idPhotoUrl':f'fsimg://applicantFiles/{u}/files/id_card','bodyPhotoUrl':f'fsimg://applicantFiles/{u}/files/body_photo',
 'status':'new','consent':True,'createdAt':1,'lang':'fa','phoneCountry':'TR','phoneLocal':'','paymentStatus':'',
 'paymentMethod':'','paymentReference':'','paymentReceiptUrl':'','undertaking':{'signed':True,'signedAt':1,'fee':500}}
st,d=patch(f'applicants/{u}',app,t); chk('create applicant record (no paymentAt)',st,d,True)
st,d=req(f'{FS}/applicants/{u}',None,t); chk('read own record',st,d,True)
prof={k:x for k,x in app.items() if k not in ('status','paymentStatus','paymentAt','paymentMethod','paymentReference','paymentReceiptUrl')}
prof['firstName']='TEST2'; prof['spokenLangs']=['fa','tr']
st,d=patch(f'applicants/{u}',prof,t,mask=list(prof.keys())); chk('profile edit (merge, no payment/status)',st,d,True)
st,d=req(f'{FS}/applicantFiles/{u}/files/payment_receipt',doc(u,150_000,'payment_receipt'),t,'PATCH'); chk('upload payment receipt photo',st,d,True)
pay={'paymentStatus':'pending','paymentMethod':'manual','paymentReference':'TR12345','paymentReceiptUrl':f'fsimg://applicantFiles/{u}/files/payment_receipt'}
st,d=patch(f'applicants/{u}',pay,t,mask=list(pay)); chk('submit manual receipt (pending)',st,d,True)
st,d=patch(f'applicants/{u}',{'paymentStatus':'paid'},t,mask=['paymentStatus']); chk('SECURITY: applicant marks self paid → deny',st,d,False)
st,d=patch(f'applicants/{u}',{'paymentAt':123},t,mask=['paymentAt']); chk('SECURITY: applicant writes paymentAt → deny',st,d,False)
t2,u2=anon()
st,d=req(f'{FS}/applicants/{u}',None,t2); chk('SECURITY: other user reads record → deny',st,d,False)
st,d=req(f'{FS}/applicantFiles/{u}/files/id_card',None,t2); chk('SECURITY: other user reads passport → deny',st,d,False)
for kind in ['id_card','body_photo','payment_receipt']: req(f'{FS}/applicantFiles/{u}/files/{kind}',None,t,'DELETE')
for tk in (t,t2): req(f'https://identitytoolkit.googleapis.com/v1/accounts:delete?key={KEY}',{'idToken':tk})
print('RESULT:', 'PASS' if all(res) else 'FAIL', f'({sum(res)}/{len(res)}) | leftover test record: "TEST2 DELETE-ME" uid {u[:6]}… (admin can delete)')
