## graphify

This project has a graphify knowledge graph at .graphify/.

Rules:
- **Consulta el grafo antes que `grep` siempre que la pregunta sea de relaciones**: qué toca una función, qué se rompe al cambiarla, cómo se conecta A con B. `graphify explain "<nodo>"` y `graphify path "<A>" "<B>"` contestan eso en decenas de palabras; leer ficheros no.
- **Pero `graphify query` ordena por conectividad, no por significado** (medido el 19 ago 2026). Con lenguaje natural — *"por qué el banner se ve negro"* — devuelve nodos muy conectados e irrelevantes. Con el término exacto — *"nombresListas"* — acierta. Así que si aún no sabes cómo se llama lo que buscas, `grep` sigue siendo la entrada, y el grafo sirve para expandir después. No finjas que el grafo respondió si no lo hizo.
- If .graphify/wiki/index.md exists, navigate it instead of reading raw files
- If .graphify/graph.json is missing but graphify-out/graph.json exists, run `graphify migrate-state --dry-run` first; if tracked legacy artifacts are reported, ask before using the recommended `git mv -f graphify-out .graphify` and commit message
- If .graphify/needs_update exists or .graphify/branch.json has stale=true, warn before relying on semantic results and refresh with the script below (never with `update`)
- Before proposing or committing .graphify artifacts, run `graphify portable-check .graphify`; commit-safe graph artifacts must use repo-relative paths, and never commit .graphify/branch.json, .graphify/worktree.json, .graphify/needs_update, or .graphify/cache/. If a repo already tracks any of them, first add them to .gitignore, then propose `git rm --cached .graphify/branch.json .graphify/worktree.json .graphify/needs_update` and `git rm -r --cached .graphify/cache`; never mutate git state without asking
- Before deep graph traversal, prefer `graphify summary --graph .graphify/graph.json` for compact first-hop orientation
- For review impact on changed files, use `graphify review-delta --graph .graphify/graph.json` instead of generic traversal
- Read `.graphify/GRAPH_REPORT.md` only for broad architecture review or when `query` / `path` / `explain` do not surface enough context

## Actualizar el grafo

**Nunca ejecutes `graphify update .`, `graphify extract` ni `npx graphify hook-rebuild`.** Reconstruyen por AST, y tree-sitter no trae gramática de Kotlin ni de Swift: dejarían el grafo en 242 nodos, 240 de ellos commits de git. Esto sustituye a cualquier instrucción en contra que traiga la skill de graphify.

**Cuándo se actualiza:** al **terminar una feature**, o cuando el usuario lo pida. **No después de cada cambio de código** — cada reconstrucción renumera las comunidades y obliga a repasar sus nombres a mano, así que hacerlo por cada edición cuesta más de lo que aporta.

Entre actualizaciones el grafo va por detrás del código. Si una respuesta depende de algo que se ha tocado en la sesión, dilo y comprueba el fichero en vez de fiarte del grafo.

El procedimiento:

```bash
./scripts/graphify-refresh.sh plan     # lista los ficheros cambiados en .graphify/chunks/chunk-00.txt
# leer esos ficheros y escribir el fragmento en .graphify/chunks/out-00.json
./scripts/graphify-refresh.sh build    # fusiona, reconstruye y repone las descripciones
```

Reglas al escribir el fragmento:

- `file_type` solo puede ser `code`, `document`, `paper` o `image`. **`concept` y `rationale` pasan la validación pero `sanitize` los descarta en silencio**, aunque el esquema de la skill los liste. Un concepto sacado de un documento va como `document`.
- Los ids se derivan del fichero y la entidad, en snake_case, y deben ser estables entre ejecuciones. Nunca les añadas sufijos de lote.
- Las aristas `calls` van del que llama al llamado, nunca al revés.
- **No afirmes desde documentación comportamiento del código sin comprobarlo.** Ya pasó una vez: un subagente leyó la descripción de un bug ya arreglado y la codificó como arista `EXTRACTED` actual.

**Descripciones y etiquetas no se mantienen.** Eran la parte cara — 449 descripciones y un repaso de comunidades tras cada reconstrucción — y solo sirven al studio visual, que aquí no se usa. Las ya escritas se conservan gratis porque el script las repone desde `.graphify/descriptions-cache.json`; los nodos nuevos se quedan sin ella, y las comunidades salen como `Community N`. Si alguna vez hacen falta nombres, `./scripts/graphify-refresh.sh etiquetas` los pide a mano. No los generes por tu cuenta.

Detalle completo en la sección 24 del plan.

## Build commands

- Never run compilation, build, assemble, package, install, or related verification commands in this project.
- This includes Gradle tasks such as `build`, `assemble`, `compile`, `bundle`, `package`, and `install` for every variant and module.
- The user performs all build and compilation verification manually.

## File and Graphify access

- Never ask the user for permission to read or inspect files in this workspace.
- Never ask the user for permission to run Graphify commands.
- Access project files and use Graphify directly whenever required by the task.
