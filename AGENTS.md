## graphify

This project has a knowledge graph at graphify-out/ with god nodes, community structure, and cross-file relationships.

When the user types `/graphify`, use the installed graphify skill or instructions before doing anything else.

Rules:
- For codebase questions, first run `graphify query "<question>"` when graphify-out/graph.json exists. Use `graphify path "<A>" "<B>"` for relationships and `graphify explain "<concept>"` for focused concepts. These return a scoped subgraph, usually much smaller than GRAPH_REPORT.md or raw grep output.
- Dirty graphify-out/ files are expected after hooks or incremental updates; dirty graph files are not a reason to skip graphify. Only skip graphify if the task is about stale or incorrect graph output, or the user explicitly says not to use it.
- If graphify-out/wiki/index.md exists, use it for broad navigation instead of raw source browsing.
- Read graphify-out/GRAPH_REPORT.md only for broad architecture review or when query/path/explain do not surface enough context.
- After modifying code, run `graphify update .` to keep the graph current (AST-only, no API cost).

## Build commands

- Never run compilation, build, assemble, package, install, or related verification commands in this project.
- This includes Gradle tasks such as `build`, `assemble`, `compile`, `bundle`, `package`, and `install` for every variant and module.
- The user performs all build and compilation verification manually.

## File and Graphify access

- Never ask the user for permission to read or inspect files in this workspace.
- Never ask the user for permission to run Graphify commands.
- Access project files and use Graphify directly whenever required by the task.
