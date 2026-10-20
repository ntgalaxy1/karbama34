# Builds Karbama/app/src/main/assets/routes/tr.json from OpenStreetMap extracts (© OpenStreetMap contributors, ODbL)
import json, math, re, collections
CITIES = {  # id: (tr, fa, en, ar, lat, lon)
 'istanbul':('İstanbul','استانبول','Istanbul','إسطنبول',41.01,28.97),
 'ankara':('Ankara','آنکارا','Ankara','أنقرة',39.93,32.86),
 'izmir':('İzmir','ازمیر','Izmir','إزمير',38.42,27.14),
 'antalya':('Antalya','آنتالیا','Antalya','أنطاليا',36.89,30.71),
 'bursa':('Bursa','بورسا','Bursa','بورصة',40.19,29.06),
 'adana':('Adana','آدانا','Adana','أضنة',37.00,35.32),
 'konya':('Konya','قونیه','Konya','قونية',37.87,32.48),
 'kayseri':('Kayseri','قیصریه','Kayseri','قيصري',38.72,35.49),
 'eskisehir':('Eskişehir','اسکی‌شهیر','Eskisehir','إسكي شهير',39.78,30.52),
 'gaziantep':('Gaziantep','غازی‌عنتاب','Gaziantep','غازي عنتاب',37.07,37.38),
 'kocaeli':('Kocaeli','کوجاائلی','Kocaeli','كوجالي',40.77,29.92),
 'samsun':('Samsun','سامسون','Samsun','سامسون',41.29,36.33),
 'bilecik':('Bilecik (Bozüyük)','بیله‌جیک (بوزویوک)','Bilecik (Bozuyuk)','بيله جك (بوزويوك)',39.91,30.05),
 'aydin':('Aydın','آیدین','Aydin','آيدين',37.85,27.85),
 'adiyaman':('Adıyaman','آدیامان','Adiyaman','أديامان',37.76,38.28),
 'agri':('Ağrı','آغری','Agri','أغري',39.72,43.05),
 'amasya':('Amasya','آماسیا','Amasya','أماصيا',40.65,35.83),
 'balikesir':('Balıkesir','بالیکسیر','Balikesir','باليكسير',39.65,27.88),
 'batman':('Batman','باتمان','Batman','باتمان',37.88,41.13),
 'bingol':('Bingöl','بینگول','Bingol','بينغول',38.88,40.50),
 'canakkale':('Çanakkale','چاناکاله','Canakkale','تشاناكالي',40.15,26.41),
 'denizli':('Denizli','دنیزلی','Denizli','دنيزلي',37.78,29.09),
 'diyarbakir':('Diyarbakır','دیاربکر','Diyarbakir','ديار بكر',37.91,40.23),
 'elazig':('Elazığ','الازیغ','Elazig','إلازيغ',38.67,39.22),
 'erzincan':('Erzincan','ارزنجان','Erzincan','أرزينجان',39.75,39.49),
 'erzurum':('Erzurum','ارزروم','Erzurum','أرضروم',39.90,41.27),
 'hakkari':('Hakkari (Yüksekova)','هکاری (یوکسک‌اووا)','Hakkari (Yuksekova)','هكاري (يوكسك أوفا)',37.57,44.28),
 'hatay':('Hatay','هاتای','Hatay','هاتاي',36.20,36.16),
 'igdir':('Iğdır','ایغدیر','Igdir','إغدير',39.92,44.04),
 'isparta':('Isparta','اسپارتا','Isparta','إسبرطة',37.76,30.55),
 'kahramanmaras':('Kahramanmaraş','قهرمان‌مرعش','Kahramanmaras','كهرمان مرعش',37.58,36.94),
 'kars':('Kars','قارص','Kars','قارص',40.60,43.10),
 'kastamonu':('Kastamonu','کاستامونو','Kastamonu','قسطموني',41.38,33.78),
 'kutahya':('Kütahya (Zafer)','کوتاهیا (ظفر)','Kutahya (Zafer)','كوتاهية (ظفر)',39.42,29.98),
 'malatya':('Malatya','ملاطیه','Malatya','ملاطية',38.35,38.31),
 'mardin':('Mardin','ماردین','Mardin','ماردين',37.31,40.74),
 'mugla':('Muğla (Bodrum · Dalaman)','موغلا (بدروم · دالامان)','Mugla (Bodrum · Dalaman)','موغلا (بودروم · دالامان)',37.22,28.36),
 'mus':('Muş','موش','Mus','موش',38.74,41.49),
 'nevsehir':('Nevşehir (Kapadokya)','نوشهیر (کاپادوکیه)','Nevsehir (Cappadocia)','نوشهر (كابادوكيا)',38.62,34.71),
 'ordu':('Ordu · Giresun','اردو · گیرسون','Ordu · Giresun','أوردو · غيرسون',40.98,37.88),
 'rize':('Rize · Artvin','ریزه · آرتوین','Rize · Artvin','ريزة · أرتفين',41.02,40.52),
 'siirt':('Siirt','سیرت','Siirt','سيرت',37.93,41.94),
 'sinop':('Sinop','سینوپ','Sinop','سينوب',42.03,35.15),
 'sivas':('Sivas','سیواس','Sivas','سيواس',39.75,37.02),
 'sanliurfa':('Şanlıurfa','شانلی‌اورفا','Sanliurfa','شانلي أورفا',37.17,38.80),
 'sirnak':('Şırnak','شیرناک','Sirnak','شرناق',37.52,42.46),
 'tekirdag':('Tekirdağ (Çorlu)','تکیرداغ (چورلو)','Tekirdag (Corlu)','تكيرداغ (تشورلو)',41.16,27.80),
 'tokat':('Tokat','توکات','Tokat','توقات',40.31,36.55),
 'trabzon':('Trabzon','ترابزون','Trabzon','طرابزون',41.00,39.72),
 'van':('Van','وان','Van','وان',38.50,43.38),
 'zonguldak':('Zonguldak','زونگولداک','Zonguldak','زونغولداك',41.45,31.79),
}
# curated: civil passenger airports in operation (closed / military / duplicate entries removed)
AIR = {
 'IST':('istanbul','İstanbul Havalimanı'),'SAW':('istanbul','İstanbul Sabiha Gökçen Havalimanı'),
 'ESB':('ankara','Ankara Esenboğa Havalimanı'),'ADB':('izmir','İzmir Adnan Menderes Havalimanı'),
 'AYT':('antalya','Antalya Havalimanı'),'GZP':('antalya','Gazipaşa-Alanya Havalimanı'),
 'YEI':('bursa','Bursa Yenişehir Havalimanı'),'COV':('adana','Çukurova Uluslararası Havalimanı (Tarsus)'),
 'KYA':('konya','Konya Havalimanı'),'ASR':('kayseri','Kayseri Erkilet Havalimanı'),
 'AOE':('eskisehir','Eskişehir Hasan Polatkan Havalimanı'),'GZT':('gaziantep','Gaziantep Havalimanı'),
 'SZF':('samsun','Samsun Çarşamba Havalimanı'),'ADF':('adiyaman','Adıyaman Havalimanı'),'AJI':('agri','Ağrı Ahmed-i Hani Havalimanı'),
 'MZH':('amasya','Amasya Merzifon Havalimanı'),'EDO':('balikesir','Balıkesir Koca Seyit Havalimanı (Edremit)'),
 'BAL':('batman','Batman Havalimanı'),'BGG':('bingol','Bingöl Havalimanı'),'CKZ':('canakkale','Çanakkale Havalimanı'),
 'DNZ':('denizli','Denizli Çardak Havalimanı'),'DIY':('diyarbakir','Diyarbakır Havalimanı'),'EZS':('elazig','Elazığ Havalimanı'),
 'ERC':('erzincan','Erzincan Yıldırım Akbulut Havalimanı'),'ERZ':('erzurum','Erzurum Havalimanı'),
 'YKO':('hakkari','Hakkari Yüksekova Selahaddin Eyyubi Havalimanı'),'HTY':('hatay','Hatay Havalimanı'),
 'IGD':('igdir','Iğdır Şehit Bülent Aydın Havalimanı'),'ISE':('isparta','Isparta Süleyman Demirel Havalimanı'),
 'KCM':('kahramanmaras','Kahramanmaraş Havalimanı'),'KSY':('kars','Kars Harakani Havalimanı'),'KFS':('kastamonu','Kastamonu Havalimanı'),
 'KZR':('kutahya','Zafer Havalimanı'),'MLX':('malatya','Malatya Havalimanı'),'MQM':('mardin','Mardin Prof. Dr. Aziz Sancar Havalimanı'),
 'BJV':('mugla','Milas-Bodrum Havalimanı'),'DLM':('mugla','Dalaman Havalimanı'),'MSR':('mus','Muş Sultan Alparslan Havalimanı'),
 'NAV':('nevsehir','Nevşehir Kapadokya Havalimanı'),'OGU':('ordu','Ordu-Giresun Havalimanı'),'RZV':('rize','Rize-Artvin Havalimanı'),
 'SXZ':('siirt','Siirt Havalimanı'),'NOP':('sinop','Sinop Havalimanı'),'VAS':('sivas','Sivas Nuri Demirağ Havalimanı'),
 'GNY':('sanliurfa','Şanlıurfa GAP Havalimanı'),'NKT':('sirnak','Şırnak Şerafettin Elçi Havalimanı'),
 'TEQ':('tekirdag','Tekirdağ Çorlu Atatürk Havalimanı'),'TJK':('tokat','Tokat Havalimanı'),'TZX':('trabzon','Trabzon Havalimanı'),
 'VAN':('van','Van Ferit Melen Havalimanı'),'ONQ':('zonguldak','Zonguldak Çaycuma Havalimanı'),
}
NAMED={'red':'#E3262B','blue':'#1F5BB8','green':'#2E9E48','orange':'#F28C28','black':'#2B2B2B','yellow':'#E8C21A','deepskyblue':'#00A9E0'}
KCOL={'metro':'#0E7FC2','tram':'#E07B18','metrobus':'#D22630','rail':'#5B6770','funicular':'#7C7358'}
EXCLUDE_WD={'Q130379086'}   # Üsküdar–Kadıköy–Maltepe tram: project, not in service
def city_of(lat,lon):
    best=None
    for cid,c in CITIES.items():
        d=math.hypot((lat-c[4])*111,(lon-c[5])*85)
        if best is None or d<best[0]: best=(d,cid)
    return best[1] if best[0]<90 else None
def col(t,kind):
    c=t.get('colour','').strip()
    if re.fullmatch(r'#[0-9A-Fa-f]{6}',c): return c.upper()
    return NAMED.get(c.lower(),KCOL[kind])
def clean(n): return re.sub(r'\s+',' ',n).strip()

groups=collections.OrderedDict()
def take(fname,kind_of,only=None):
    d=json.load(open(fname)); nodes={e['id']:e for e in d['elements'] if e['type']=='node'}
    for r in d['elements']:
        if r['type']!='relation' or r['tags'].get('type')!='route': continue
        t=r['tags']; name=t.get('name','')
        if only and not only(t): continue
        if t.get('wikidata') in EXCLUDE_WD: continue
        if any(t.get(k) for k in ('state','proposed','construction','disused')) or re.search(r'Proje|Güzergah',name): continue
        st=[]
        for m in r['members']:
            if m['type']=='node' and m['role'].startswith('stop') and m['ref'] in nodes:
                n=nodes[m['ref']]; nm=clean(n.get('tags',{}).get('name','') or n.get('tags',{}).get('name:tr',''))
                if nm and (not st or st[-1][0]!=nm): st.append([nm,round(n['lat'],5),round(n['lon'],5)])
        if len(st)<2: continue
        la=sum(s[1] for s in st)/len(st); lo=sum(s[2] for s in st)/len(st)
        if lo>44.0 or lat_bad(la): continue
        cid=city_of(la,lo)
        if not cid: continue
        kind=kind_of(t)
        ref=t.get('ref') or ''
        if cid=='kayseri': ref=re.sub(r'a$','',ref)
        if 'AYBAN' in name: ref='AYBAN'
        if ref=='İZBAN' or 'İZBAN' in name: cid='izmir'; ref='İZBAN'
        if cid=='bursa' and ref=='T1':
            st=[[re.sub(r'^T1-','',x[0])]+x[1:] for x in st]
        if cid=='istanbul' and ref.startswith('F'): kind='funicular'
        if cid=='izmir' and 'Mavişehir' in name: continue          # unclear 3-stop stub
        if t.get('network','').startswith('TCDD Bölgesel'): continue  # Halkalı–Bahçeşehir: not confirmed in service
        key=(cid,kind,ref or name.split(':')[0])
        groups.setdefault(key,[]).append((t,st))
def lat_bad(la): return la<35.8 or la>42.2
take('raw-subway.json',lambda t:'metro')
take('raw-light_rail.json',lambda t:'metro')
take('raw-tram.json',lambda t:'tram')
take('raw-other.json',lambda t:'funicular')
take('raw-train.json',lambda t:'rail')
take('raw-mb34.json',lambda t:'metrobus',only=lambda t:t.get('ref')=='34G')

out={}
for (cid,kind,ref),vs in groups.items():
    vs.sort(key=lambda v:-len(v[1])); t,main=vs[0]; main=[s[:] for s in main]
    title=f"{t['from']} – {t['to']}" if t.get('from') and t.get('to') and t['from']!=t['to'] else (
          f"{main[0][0]} ↻" if main[0][0]==main[-1][0] else f"{main[0][0]} – {main[-1][0]}")
    names={s[0] for s in main}
    for t2,st in vs[1:]:            # branches / extensions: attach at the matching end
        extra=[s for s in st if s[0] not in names]
        if not extra: continue
        if st[-1][0]==main[-1][0]:
            main=main+extra[::-1]
        elif st[-1][0]==main[0][0] or st[0][0]==main[0][0]:
            ex=extra if st[-1][0]==main[0][0] else extra[::-1]; main=ex+main
        else:
            ex=extra if st[0][0]==main[-1][0] else extra; main=main+ex
        names|={s[0] for s in extra}
    title={('istanbul','T3'):'Kadıköy – Moda'}.get((cid,ref),title)
    if kind=='metrobus': ref='34'
    out.setdefault(cid,[]).append({'k':kind,'r':ref if len(ref)<=6 else '','c':col(t,kind),'n':title,'s':main})
# manual: Tünel (F2) has no stop members in OSM
out['istanbul'].append({'k':'funicular','r':'F2','c':'#7C7358','n':'Karaköy – Beyoğlu (Tünel)','s':[['Karaköy',41.02234,28.97418],['Beyoğlu (Tünel)',41.02866,28.97383]]})

air={}
d=json.load(open('raw-air.json'))
for e in d['elements']:
    t=e.get('tags',{}); i=t.get('iata')
    if i not in AIR or 'Üs' in t.get('name','') or (t.get('military')=='airfield' and i!='ASR'): continue
    c=e.get('center',e); cid,nm=AIR[i]
    if any(a[1]==i for a in air.get(cid,[])): continue
    air.setdefault(cid,[]).append([nm,i,round(c['lat'],5),round(c['lon'],5)])
missing=set(AIR)-{a[1] for v in air.values() for a in v}
print('missing airports:',missing)

KORD={'metro':0,'metrobus':1,'tram':2,'rail':3,'funicular':4}
def rkey(l): 
    m=re.match(r'([A-Za-zİ]*)(\d*)(.*)',l['r'] or 'Z'); return (KORD[l['k']],m.group(1),int(m.group(2) or 0),m.group(3),l['n'])
order=['istanbul','ankara','izmir','antalya','bursa']
cids=[c for c in order if c in out or c in air]+sorted([c for c in CITIES if c not in order and (c in out or c in air)],key=lambda c:CITIES[c][0].replace('Ç','C').replace('Ş','S').replace('İ','I').replace('Ö','O').replace('Ü','U'))
res={'v':1,'country':'TR','updated':'2026-10-20','src':'© OpenStreetMap contributors (ODbL)','cities':[]}
for cid in cids:
    c=CITIES[cid]
    res['cities'].append({'id':cid,'n':{'tr':c[0],'fa':c[1],'en':c[2],'ar':c[3]},'lat':c[4],'lon':c[5],
        'air':air.get(cid,[]),'lines':sorted(out.get(cid,[]),key=rkey)})
s=json.dumps(res,ensure_ascii=False,separators=(',',':'))
import os; os.makedirs('/home/user/Karbama/app/src/main/assets/routes',exist_ok=True)
open('/home/user/Karbama/app/src/main/assets/routes/tr.json','w',encoding='utf-8').write(s)
print('bytes',len(s.encode()),'cities',len(res['cities']),'airports',sum(len(c['air']) for c in res['cities']),'lines',sum(len(c['lines']) for c in res['cities']),'stations',sum(len(l['s']) for c in res['cities'] for l in c['lines']))
for c in res['cities']:
    if c['lines']: print(c['id'], ' | '.join(f"{l['k'][:4]}:{l['r']}({len(l['s'])}) {l['n'][:34]}" for l in c['lines']))
