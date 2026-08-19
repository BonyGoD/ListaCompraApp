## graphify

This project has a knowledge graph at .graphify/ with god nodes, community structure, and cross-file relationships.

When the user types `/graphify`, use the installed graphify skill or instructions before doing anything else.

Rules:
- Consulta el grafo antes que `grep` cuando la pregunta sea de relaciones: `graphify explain "<nodo>"` y `graphify path "<A>" "<B>"` responden qué toca una función o cómo conecta con otra, cosa que grep no puede.
- `graphify query` ordena por conectividad, no por significado: acierta con el término exacto y falla con lenguaje natural. Si aún no sabes cómo se llama lo que buscas, entra por `grep` y usa el grafo para expandir.
- Dirty .graphify/ files are expected after hooks or incremental updates; dirty graph files are not a reason to skip graphify. Only skip graphify if the task is about stale or incorrect graph output, or the user explicitly says not to use it.
- If .graphify/wiki/index.md exists, use it for broad navigation instead of raw source browsing.
- Read .graphify/GRAPH_REPORT.md only for broad architecture review or when query/path/explain do not surface enough context.
- **Nunca ejecutes `graphify update .` ni `hook-rebuild`**: reconstruyen por AST y tree-sitter no trae gramática de Kotlin ni de Swift, así que dejarían el grafo en 242 nodos, 240 de ellos commits de git.
- El grafo se actualiza **al terminar una feature o cuando el usuario lo pida**, no tras cada cambio de código: `./scripts/graphify-refresh.sh plan` y `./scripts/graphify-refresh.sh build`. Entre actualizaciones va por detrás del código; si la respuesta depende de algo tocado en la sesión, comprueba el fichero. Las descripciones de nodo y los nombres de comunidad **no se mantienen**: son el grueso del coste y solo sirven al studio visual. Reglas del fragmento: ver CLAUDE.md y la sección 24 del plan.

## Build commands

- Never run compilation, build, assemble, package, install, or related verification commands in this project.
- This includes Gradle tasks such as `build`, `assemble`, `compile`, `bundle`, `package`, and `install` for every variant and module.
- The user performs all build and compilation verification manually.

## File and Graphify access

- Never ask the user for permission to read or inspect files in this workspace.
- Never ask the user for permission to run Graphify commands.
- Access project files and use Graphify directly whenever required by the task.
