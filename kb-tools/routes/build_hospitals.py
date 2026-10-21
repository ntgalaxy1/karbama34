#!/usr/bin/env python3
"""Build a compact, curated hospital list per bundled Turkish route city from Overpass JSON.
Source data: © OpenStreetMap contributors, ODbL.  Kept separate from tr.json so existing
airport/urban-transit generation remains reproducible unchanged.
"""
from __future__ import annotations
import json, math
from pathlib import Path
ROOT=Path(__file__).resolve().parent
ROUTES=ROOT/'../../Karbama/app/src/main/assets/routes/tr.json'
RAW=ROOT/'tr-hospitals-overpass-2026-10-21.json'
OUT=ROOT/'../../Karbama/app/src/main/java/com/karbama/app/data/routes/TurkeyHospitals.kt'

def distance(a,b,c,d):
    # Haversine km
    r=6371.0088; p=math.pi/180
    x=math.sin((c-a)*p/2)**2+math.cos(a*p)*math.cos(c*p)*math.sin((d-b)*p/2)**2
    return 2*r*math.asin(min(1, math.sqrt(x)))

def point(e):
    if 'lat' in e: return e['lat'],e['lon']
    c=e.get('center',{}); return c.get('lat'),c.get('lon')

def esc(s): return s.replace('\\','\\\\').replace('"','\\"').replace('\n',' ')

cities=json.load(open(ROUTES,encoding='utf8'))['cities']
raw=json.load(open(RAW,encoding='utf8'))['elements']
h=[]
for e in raw:
    t=e.get('tags',{}); name=(t.get('name') or t.get('name:en') or '').strip()
    lat,lon=point(e)
    if not name or lat is None or lon is None: continue
    h.append((name,float(lat),float(lon)))

result={}
for city in cities:
    candidates=[]
    for name,lat,lon in h:
        km=distance(city['lat'],city['lon'],lat,lon)
        if km <= 55: candidates.append((km,name,lat,lon))
    candidates.sort(key=lambda x:(x[0],x[1]))
    selected=[]; seen=set()
    for km,name,lat,lon in candidates:
        key=' '.join(name.casefold().split())
        # avoid duplicate OSM node/way records and practically identical points
        if key in seen or any(distance(lat,lon,x[2],x[3])<.25 for x in selected): continue
        seen.add(key); selected.append((km,name,lat,lon))
        if len(selected)==6: break
    result[city['id']]=selected

lines=['package com.karbama.app.data.routes','',
       '/**',
       ' * بیمارستان‌های نزدیک مراکز شهرهای دادهٔ مسیریابی ترکیه.',
       ' * دادهٔ خام: OpenStreetMap / Overpass، 2026-10-21، © OpenStreetMap contributors (ODbL).',
       ' * برای کوچک ماندن APK و خوانایی، حداکثر شش مرکز نام‌دار برای هر شهر انتخاب شده است.',
       ' */','object TurkeyHospitals {','    private val byCity: Map<String, List<RoutePlace>> = mapOf(']
for index,(city,items) in enumerate(result.items()):
    rows=', '.join(f'RoutePlace("{esc(n)}", {lat:.6f}, {lon:.6f})' for _,n,lat,lon in items)
    comma=',' if index<len(result)-1 else ''
    lines.append(f'        "{city}" to listOf({rows}){comma}')
lines.extend(['    )','', '    fun forCity(cityId: String): List<RoutePlace> = byCity[cityId].orEmpty()', '}',''])
OUT.parent.mkdir(parents=True,exist_ok=True); OUT.write_text('\n'.join(lines),encoding='utf8')
print('cities',len(result),'places',sum(map(len,result.values())),'empty',sum(not v for v in result.values()))
