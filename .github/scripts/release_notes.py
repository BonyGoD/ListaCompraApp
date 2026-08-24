"""Genera las notas de version a partir de los commits que entran en la release.

Lo usa `.github/workflows/generate_release.yml`, y **es el mismo archivo que puedes
ejecutar en local** para ver exactamente lo que se va a publicar antes de mergear:

    python .github/scripts/release_notes.py

Sin argumentos se apana solo: coge la version de `androidApp/build.gradle.kts`, el
tag `v*` mas alto que exista como version anterior, y lista lo que hay entre ese tag
y HEAD. Tambien acepta un rango explicito:

    python .github/scripts/release_notes.py v1.3.0..release/1.4.0

Por que el rango y no la API de PRs (24 ago 2026). Antes se listaban las PRs con
base `develop` mergeadas despues del `published_at` del ultimo release. Fallaba por
los dos lados:

  - La rama de release se corta de develop y develop sigue avanzando. Todo lo que se
    mergeara ahi mientras la release esperaba a produccion salia en las notas **sin
    estar en el binario**.
  - Los commits directos a la rama de release no pasan por ninguna PR a develop, asi
    que no aparecian. En la 1.4.0 eran dos `fix:` de verdad.
  - Y el filtro por fecha iba al limite: la PR #57 entro en el changelog de la 1.4.0
    por **14 segundos** sobre el `published_at` de la v1.3.0.

El rango de commits es, por definicion, lo que se publica.
"""

import os
import re
import subprocess
import sys

# Prefijo de conventional commit -> titulo de seccion. `chore` no esta a proposito:
# "subir versionName a 1.4.0" no es una nota de version, el numero ya sale en el
# titulo del release.
SECTIONS = [
    ("feat", "Novedades"),
    ("fix", "Arreglos"),
    ("perf", "Rendimiento"),
    ("refactor", "Interno"),
    ("docs", "Documentación"),
]

CONVENTIONAL = re.compile(r"(\w+)(\([^)]*\))?(!?):\s*(.+)")


def git(*args: str) -> str:
    return subprocess.run(
        ["git", *args],
        capture_output=True,
        text=True,
        check=True,
        encoding="utf-8",
        errors="replace",
    ).stdout.strip()


def read(path: str) -> str:
    try:
        with open(path, encoding="utf-8") as handle:
            return handle.read()
    except OSError:
        return ""


def find(pattern: str, text: str, default: str = "?") -> str:
    match = re.search(pattern, text)
    return match.group(1).strip() if match else default


def latest_tag() -> str:
    """El tag v* mas alto del repo.

    No vale `git describe`: los tags viven en `main` y las ramas de release salen de
    `develop`, asi que **ningun tag de release es antecesor de la rama de release**.
    `describe` no encontraria ninguno.
    """
    tags = git("tag", "--sort=-v:refname").splitlines()
    for tag in tags:
        if re.match(r"^v\d", tag):
            return tag
    return ""


def build_notes(rng: str, new: str, prev: str, repo: str) -> str:
    buckets: dict[str, list[str]] = {key: [] for key, _ in SECTIONS}
    breaking: list[str] = []
    seen: set[str] = set()

    for line in git("log", "--no-merges", "--format=%s", rng).splitlines():
        match = CONVENTIONAL.match(line.strip())
        if not match:
            continue
        kind, _, bang, message = match.groups()
        message = message.strip()
        # Un mismo arreglo cherry-pickeado aparece dos veces con el mismo asunto
        if message.lower() in seen:
            continue
        seen.add(message.lower())
        entry = message[0].upper() + message[1:]
        if bang:
            breaking.append(entry)
        if kind in buckets:
            buckets[kind].append(entry)

    out: list[str] = []

    # Si existe `.github/release-notes/<version>.md`, encabeza el release: es el texto
    # de cara al usuario, el mismo que va al campo "Novedades" de las tiendas. El
    # changelog tecnico va debajo. Es opcional; sin el archivo no pasa nada.
    store = read(f".github/release-notes/{new}.md").strip()
    if store:
        out += [store, "", "---", ""]

    if breaking:
        out.append("### ⚠️ Cambios que rompen compatibilidad\n")
        out += [f"- {entry}" for entry in breaking]
        out.append("")

    for key, title in SECTIONS:
        if buckets[key]:
            out.append(f"### {title}\n")
            out += [f"- {entry}" for entry in buckets[key]]
            out.append("")

    if not breaking and not any(buckets.values()):
        out += [f"- Release version v{new}", ""]

    # El dato que de verdad se consulta meses despues: que versionCode y que build de
    # iOS corresponden a este tag. Se leen del repo, asi que no pueden desincronizarse
    # de lo que se compilo.
    gradle = read("androidApp/build.gradle.kts")
    pbx = read("iosApp/iosApp.xcodeproj/project.pbxproj")

    out += [
        "---",
        "",
        "| Plataforma | Versión |",
        "|---|---|",
        f"| Android | `{new}` · versionCode `{find(r'versionCode\s*=\s*(\d+)', gradle)}` |",
        f"| iOS | `{find(r'MARKETING_VERSION = ([^;]+);', pbx)}` "
        f"· build `{find(r'CURRENT_PROJECT_VERSION = ([^;]+);', pbx)}` |",
    ]

    if prev:
        out += ["", f"**Full Changelog**: https://github.com/{repo}/compare/v{prev}...v{new}"]

    return "\n".join(out)


def main() -> None:
    sys.stdout.reconfigure(encoding="utf-8")

    gradle = read("androidApp/build.gradle.kts")
    if not gradle:
        raise SystemExit("Ejecuta esto desde la raiz del repo: no veo androidApp/build.gradle.kts")

    new = os.environ.get("NEW_VERSION") or find(r'versionName\s*=\s*"([^"]+)"', gradle, "")
    repo = os.environ.get("REPO") or "BonyGoD/Lista-compra-app"

    rng = sys.argv[1] if len(sys.argv) > 1 else os.environ.get("RANGE", "")
    prev = os.environ.get("PREV_VERSION", "")

    if not rng:
        tag = latest_tag()
        if not tag:
            raise SystemExit("No hay ningun tag v*; pasa el rango a mano")
        rng = f"{tag}..HEAD"
        prev = tag.lstrip("v")
        print(f"# rango: {rng}", file=sys.stderr)

    print(build_notes(rng, new, prev, repo))


if __name__ == "__main__":
    main()
