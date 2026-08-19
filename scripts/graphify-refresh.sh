#!/usr/bin/env bash
# Actualiza el grafo de graphify en este proyecto.
#
# No uses `graphify update .` ni `hook-rebuild`: reconstruyen por AST, y
# tree-sitter no trae gramática de Kotlin ni de Swift. El grafo se quedaría en
# 242 nodos, 240 de ellos commits de git. Ver sección 24 del plan.
#
#   ./scripts/graphify-refresh.sh plan       -> qué ficheros han cambiado
#   ./scripts/graphify-refresh.sh build      -> fusiona y reconstruye
#   ./scripts/graphify-refresh.sh etiquetas  -> renombrar comunidades (a mano, opcional)
#
# Descripciones y etiquetas NO se mantienen: son la parte cara y solo sirven al
# studio visual. Las descripciones ya escritas se conservan gratis; los nodos
# nuevos se quedan sin ella y las comunidades salen como "Community N".
#
# Entre los dos pasos, el asistente extrae los ficheros que liste `plan` y deja
# un fragmento JSON por lote en .graphify/chunks/out-*.json.

set -euo pipefail
cd "$(dirname "$0")/.."
export NODE_PATH="$(npm root -g)"

case "${1:-}" in
plan)
    graphify detect . --scope auto --out .graphify/.graphify_detect.json >/dev/null
    node -e "
    const fs=require('fs');
    const { checkSemanticCache } = require('@sentropic/graphify');
    const d=JSON.parse(fs.readFileSync('.graphify/.graphify_detect.json','utf-8'));
    const f=d.files||{};
    const list=[...(f.code||[]),...(f.document||[])]
        .map(p=>require('path').relative(process.cwd(),p))
        .filter(p=>!p.startsWith('.graphify/'))
        // fuera lo que no da fragmento semantico: los wrappers de Gradle y lo que
        // ya resuelve el AST por su cuenta
        .filter(p=>!/^gradlew(\.bat)?\$/.test(p))
        .filter(p=>!/\.(js|ts|tsx|jsx|py|java|go|rs|rb|c|h|cpp|hpp|cs|php)\$/.test(p));
    const [,,,uncached]=checkSemanticCache(list);
    fs.mkdirSync('.graphify/chunks',{recursive:true});
    if(uncached.length===0){ console.log('Nada que extraer: el grafo esta al dia.'); process.exit(0); }
    fs.writeFileSync('.graphify/chunks/chunk-00.txt', uncached.join('\n'));
    console.log(uncached.length+' fichero(s) cambiados, escritos en .graphify/chunks/chunk-00.txt:');
    for(const u of uncached) console.log('  '+u);
    console.log('\nSiguiente paso: que el asistente los extraiga a .graphify/chunks/out-00.json');
    "
    ;;
build)
    # 0. Salvar las descripciones que ya tiene el grafo. Reconstruir las tira, y
    #    `describe` borra los ficheros de respuesta al ingerirlos: sin esta copia
    #    se pierden las dos a la vez y hay que reescribir las 439 a mano.
    node -e "
    const fs=require('fs');
    let acc={};
    if(fs.existsSync('.graphify/descriptions-cache.json')) acc=JSON.parse(fs.readFileSync('.graphify/descriptions-cache.json','utf-8'));
    if(fs.existsSync('.graphify/graph.json')){
        for(const n of JSON.parse(fs.readFileSync('.graphify/graph.json','utf-8')).nodes||[]){
            if(n.description) acc[n.id]=n.description;
        }
    }
    fs.writeFileSync('.graphify/descriptions-cache.json', JSON.stringify(acc,null,1));
    console.log('Descripciones a salvo: '+Object.keys(acc).length);
    "

    # 1. fragmentos nuevos -> cache semantica
    node -e "
    const fs=require('fs');
    const { saveSemanticCache, validateSemanticFragment, sanitizeSemanticFragment, checkSemanticCache } = require('@sentropic/graphify');
    let nodes=[],edges=[],hyper=[];
    for(const f of fs.readdirSync('.graphify/chunks').filter(f=>/^out-\d+\.json\$/.test(f))){
        const raw=JSON.parse(fs.readFileSync('.graphify/chunks/'+f,'utf-8'));
        nodes=nodes.concat(raw.nodes||[]); edges=edges.concat(raw.edges||[]); hyper=hyper.concat(raw.hyperedges||[]);
    }
    if(nodes.length){
        const errs=validateSemanticFragment({nodes,edges,hyperedges:hyper});
        if(errs.length){ console.error('Fragmento invalido: '+errs.slice(0,3).join('; ')); process.exit(1); }
        const clean=sanitizeSemanticFragment({nodes,edges,hyperedges:hyper});
        console.log('Cacheados '+saveSemanticCache(clean.nodes,clean.edges,clean.hyperedges)+' fichero(s)');
    }
    // 2. corpus completo desde cache
    const d=JSON.parse(fs.readFileSync('.graphify/.graphify_detect.json','utf-8'));
    const f=d.files||{};
    const list=[...(f.code||[]),...(f.document||[])]
        .map(p=>require('path').relative(process.cwd(),p))
        .filter(p=>!p.startsWith('.graphify/'));
    const [cn,ce,ch]=checkSemanticCache(list);
    const seen=new Set(); const ded=[];
    for(const n of cn){ if(!seen.has(n.id)){ seen.add(n.id); ded.push(n); } }
    const kept=ce.filter(e=>seen.has(e.source)&&seen.has(e.target));
    // Rescate: hay nodos que la cache no puede reproducir — los del AST y los que
    // no tienen un fichero de origen valido. Se conservan del grafo anterior en
    // .graphify/nodes-extra.json, o desaparecerian en cada reconstruccion.
    let extra={nodes:[],edges:[]};
    if(fs.existsSync('.graphify/nodes-extra.json')) extra=JSON.parse(fs.readFileSync('.graphify/nodes-extra.json','utf-8'));
    if(fs.existsSync('.graphify/graph.json')){
        const g=JSON.parse(fs.readFileSync('.graphify/graph.json','utf-8'));
        const ex=new Set(extra.nodes.map(n=>n.id));
        for(const n of g.nodes||[]){
            if(!seen.has(n.id) && !ex.has(n.id)){
                extra.nodes.push({id:n.id,label:n.label,file_type:n.file_type||'code',source_file:n.source_file||null,source_location:n.source_location||null,rationale:null});
                ex.add(n.id);
            }
        }
        // Conservar tambien las aristas del grafo anterior entre nodos que siguen
        // existiendo. Sin esto cada refresco erosiona el grafo: un fragmento solo
        // reproduce las aristas internas a los ficheros re-extraidos, y las que
        // cruzaban hacia ficheros sin tocar se perdian en cada pasada.
        const vivos=new Set([...seen, ...ex]);
        for(const l of g.links||[]){
            const relevante = ex.has(l.source)||ex.has(l.target) || (vivos.has(l.source)&&vivos.has(l.target));
            if(relevante && !extra.edges.some(e=>e.source===l.source&&e.target===l.target&&e.relation===l.relation)){
                extra.edges.push({source:l.source,target:l.target,relation:l.relation||'references',confidence:l.confidence||'EXTRACTED',confidence_score:l.confidence_score||1.0,source_file:l.source_file||null,source_location:null,weight:1.0});
            }
        }
        fs.writeFileSync('.graphify/nodes-extra.json', JSON.stringify(extra,null,1));
    }
    for(const n of extra.nodes){ if(!seen.has(n.id)){ seen.add(n.id); ded.push(n); } }
    const allEdges=kept.concat(extra.edges.filter(e=>seen.has(e.source)&&seen.has(e.target)));
    fs.writeFileSync('.graphify/.graphify_extract.json', JSON.stringify({
        nodes:ded, edges:allEdges, hyperedges:(ch||[]).filter(h=>(h.nodes||[]).every(i=>seen.has(i))),
        input_tokens:0, output_tokens:0
    },null,2));
    console.log('Extraccion: '+ded.length+' nodos ('+extra.nodes.length+' rescatados), '+allEdges.length+' aristas');
    "

    # 3. construir grafo, comunidades e informe
    node -e "
    const fs=require('fs');
    const { buildFromJson, cluster, scoreAll, godNodes, surprisingConnections, suggestQuestions, generateReport, toJson } = require('@sentropic/graphify');
    const extraction=JSON.parse(fs.readFileSync('.graphify/.graphify_extract.json','utf-8'));
    const detection=JSON.parse(fs.readFileSync('.graphify/.graphify_detect.json','utf-8'));
    const G=buildFromJson(extraction);
    const communities=cluster(G);
    const labels=new Map(Array.from(communities.keys(), c=>[c,'Community '+c]));
    const questions=suggestQuestions(G,communities,labels);
    fs.writeFileSync('.graphify/GRAPH_REPORT.md', generateReport(G,communities,scoreAll(G,communities),labels,godNodes(G),surprisingConnections(G,communities),detection,{input:0,output:0},'.',{suggestedQuestions:questions}));
    toJson(G,communities,'.graphify/graph.json');
    if(G.order===0){ console.error('ERROR: grafo vacio'); process.exit(1); }
    // graphify se niega a sobrescribir si el grafo nuevo tiene menos nodos que el
    // guardado. Es una red util, pero hay que enterarse: sin esta comprobacion el
    // script diria que fue bien mientras graph.json sigue siendo el de antes.
    const escrito=JSON.parse(fs.readFileSync('.graphify/graph.json','utf-8')).nodes.length;
    if(escrito!==G.order){
        console.error('');
        console.error('AVISO: graph.json NO se ha actualizado.');
        console.error('  construido: '+G.order+' nodos  |  en disco: '+escrito+' nodos');
        console.error('  graphify rechaza sobrescribir con un grafo mas pequeno.');
        console.error('  Revisa si falta extraer algun fichero antes de forzar nada.');
        process.exit(2);
    }
    console.log('Grafo: '+G.order+' nodos, '+G.size+' aristas, '+communities.size+' comunidades');
    "

    # 4. Reponer las descripciones ya escritas. Esto es I/O, no cuesta tokens:
    #    se rellenan los lotes con la copia y graphify las ingiere. Los nodos
    #    nuevos se quedan sin descripcion a proposito.
    graphify describe . >/dev/null 2>&1 || true
    node -e "
    const fs=require('fs');
    const dir='.graphify/description-instructions';
    if(!fs.existsSync(dir)) process.exit(0);
    const acc=JSON.parse(fs.readFileSync('.graphify/descriptions-cache.json','utf-8'));
    let sin=[];
    for(const md of fs.readdirSync(dir).filter(f=>/^batch-\\d+\\.md\$/.test(f))){
        const ids=fs.readFileSync(dir+'/'+md,'utf-8').split('\\n').filter(l=>l.startsWith('- \"')).map(l=>l.split('\"')[1]);
        const ans={};
        for(const i of ids){ if(acc[i]) ans[i]=acc[i]; else sin.push(i); }
        fs.writeFileSync(dir+'/'+md.replace('.md','.json'), JSON.stringify(ans,null,1));
    }
    if(sin.length) console.log(sin.length+' nodo(s) nuevos sin descripcion (no se mantienen: ver CLAUDE.md)');
    "
    graphify describe . 2>&1 | grep -iE "ingested" || true

    echo
    echo "Grafo actualizado. Las comunidades salen como \"Community N\";"
    echo "si alguna vez quieres nombrarlas: ./scripts/graphify-refresh.sh etiquetas"
    ;;
etiquetas)
    # Paso opcional y manual: Louvain renumera las comunidades en cada
    # reconstruccion, asi que los nombres viejos no se pueden reutilizar sin
    # revisarlos uno a uno.
    rm -f .graphify/label-instructions/communities.json
    graphify label . >/dev/null 2>&1 || true
    echo "Comunidades actuales:"
    grep '^Community' .graphify/label-instructions/communities.md | cut -c1-100
    echo
    echo "Escribe los nombres en .graphify/label-instructions/communities.json"
    echo "y despues:  graphify label ."
    ;;
*)
    echo "uso: $0 plan|build|etiquetas"
    exit 1
    ;;
esac
