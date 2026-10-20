import urllib.request, urllib.parse, json, time, os, sys
EPS=['https://overpass.openstreetmap.fr/api/interpreter']
SEL={
 'subway':'relation["type"="route"]["route"="subway"](35.8,25.6,42.2,44.9);',
 'light_rail':'relation["type"="route"]["route"="light_rail"](35.8,25.6,42.2,44.9);',
 'tram':'relation["type"="route"]["route"="tram"](35.8,25.6,42.2,44.9);',
 'other':'relation["type"="route"]["route"~"^(monorail|funicular)$"](35.8,25.6,42.2,44.9);',
 'metrobus':'relation["type"="route"]["route"="bus"]["name"~"Metrob",i](35.8,25.6,42.2,44.9);',
 'train':'relation["type"="route"]["route"="train"]["service"~"commuter|suburban|urban"](35.8,25.6,42.2,44.9); relation["type"="route"]["route"="train"]["name"~"Marmaray|Başkentray|İZBAN|Gaziray|Banliyö",i](35.8,25.6,42.2,44.9);',
}
for k,sel in SEL.items():
    out=f'raw-{k}.json'
    if os.path.exists(out) and os.path.getsize(out)>200: print(k,'cached'); continue
    q=f'[out:json][timeout:250];({sel})->.r;.r out body;node(r.r);out body;'
    ok=False
    for attempt in range(8):
        ep=EPS[attempt%len(EPS)]
        try:
            req=urllib.request.Request(ep,data=urllib.parse.urlencode({'data':q}).encode(),headers={'User-Agent':'KarbamaDataBuild/1.0 (github.com/ntgalaxy1/karbama34)','Accept':'application/json'})
            b=urllib.request.urlopen(req,timeout=260).read()
            d=json.loads(b)
            rels=[e for e in d['elements'] if e['type']=='relation']
            open(out,'wb').write(b); print(k,'OK',ep,len(rels),'relations',flush=True); ok=True; break
        except Exception as e:
            print(k,'fail',ep,str(e)[:80],flush=True); time.sleep(15)
    if not ok: print(k,'GAVE UP',flush=True)
print('DONE')
