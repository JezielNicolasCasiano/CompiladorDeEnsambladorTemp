#!/usr/bin/env bash
# ============================================================
# build-optimized.sh
# Construye el JAR optimizado (<5 MB) y genera el ZIP de fuentes
# Estrategia:
#   1. mvn clean package  → genera jar-with-dependencies + shrunk (ProGuard)
#   2. Post-proceso       → elimina recursos no-Java innecesarios del shrunk
#   3. Reempaqueta        → JAR final
#   4. Genera ZIP de fuentes
# ============================================================
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TARGET="$PROJECT_DIR/target"
OUTPUT_JAR="$PROJECT_DIR/CompiladorDeEnsamblador.jar"
OUTPUT_ZIP="$PROJECT_DIR/CompiladorDeEnsamblador_CodigoFuente.zip"
ARTIFACT="CompiladorDeEnsamblador-1.0-SNAPSHOT"
WORK_DIR="$TARGET/jar-work"

echo "╔══════════════════════════════════════════════════════════╗"
echo "║   Paso 1: Compilando y aplicando ProGuard...            ║"
echo "╚══════════════════════════════════════════════════════════╝"
cd "$PROJECT_DIR"
./mvnw clean package -q
echo "  ✓ Maven build completado"

SHRUNK_JAR="$TARGET/${ARTIFACT}-shrunk.jar"
if [ ! -f "$SHRUNK_JAR" ]; then
    echo "ERROR: No se generó $SHRUNK_JAR"
    exit 1
fi

SHRUNK_SIZE=$(du -sh "$SHRUNK_JAR" | cut -f1)
echo "  → JAR shrunk por ProGuard: $SHRUNK_SIZE"

echo ""
echo "╔══════════════════════════════════════════════════════════╗"
echo "║   Paso 2: Eliminando recursos innecesarios...           ║"
echo "╚══════════════════════════════════════════════════════════╝"

rm -rf "$WORK_DIR"
mkdir -p "$WORK_DIR"
cd "$WORK_DIR"
jar xf "$SHRUNK_JAR"

# ── Tema Caspian completo (obsoleto, solo se usa Modena en JavaFX 8+) ──────────
echo "  → Eliminando tema Caspian..."
rm -rf "$WORK_DIR/com/sun/javafx/scene/control/skin/caspian"

# ── javafx-swt.jar (integración con SWT de Eclipse, no usada) ─────────────────
echo "  → Eliminando javafx-swt.jar..."
rm -f "$WORK_DIR/javafx-swt.jar"

# ── Temas de alto contraste de Modena (accesibilidad avanzada, no usados) ──────
echo "  → Eliminando temas de alto contraste..."
rm -f "$WORK_DIR/com/sun/javafx/scene/control/skin/modena/yellowOnBlack.css"
rm -f "$WORK_DIR/com/sun/javafx/scene/control/skin/modena/yellowOnBlack.bss"
rm -f "$WORK_DIR/com/sun/javafx/scene/control/skin/modena/whiteOnBlack.css"
rm -f "$WORK_DIR/com/sun/javafx/scene/control/skin/modena/whiteOnBlack.bss"
rm -f "$WORK_DIR/com/sun/javafx/scene/control/skin/modena/blackOnWhite.css"
rm -f "$WORK_DIR/com/sun/javafx/scene/control/skin/modena/blackOnWhite.bss"

# ── Temas touch/embedded (no es aplicación móvil ni táctil) ────────────────────
echo "  → Eliminando temas touch/embedded..."
rm -f "$WORK_DIR/com/sun/javafx/scene/control/skin/modena/touch.css"
rm -f "$WORK_DIR/com/sun/javafx/scene/control/skin/modena/touch.bss"
rm -f "$WORK_DIR/com/sun/javafx/scene/control/skin/modena/two-level-focus.css"
rm -f "$WORK_DIR/com/sun/javafx/scene/control/skin/modena/two-level-focus.bss"

# ── Shaders GLSL de convolución (efectos de blur/sombra avanzados) ─────────────
echo "  → Eliminando shaders GLSL no usados..."
rm -rf "$WORK_DIR/com/sun/scenario/effect/impl/es2/glsl"

# ── Imágenes de diálogos del sistema (Alert/Dialog, no usamos) ─────────────────
echo "  → Eliminando imágenes de diálogos del sistema..."
find "$WORK_DIR/com/sun/javafx/scene/control/skin/modena" -name "dialog-*.png" -delete 2>/dev/null || true

# ── Metadatos de compilación nativa GraalVM (no aplicables en JVM) ─────────────
echo "  → Eliminando metadatos de GraalVM substrate..."
rm -rf "$WORK_DIR/META-INF/substrate"

# ── Propiedades i18n no usadas (mantener solo las de inglés/español) ───────────
echo "  → Limpiando propiedades de idiomas no usados..."
SKIN_RES="$WORK_DIR/com/sun/javafx/scene/control/skin/resources"
if [ -d "$SKIN_RES" ]; then
    for f in "$SKIN_RES"/controls_*.properties; do
        basename_f="$(basename "$f")"
        # Conservar solo: controls.properties, controls_es.properties
        if [[ "$basename_f" != "controls.properties" && "$basename_f" != "controls_es.properties" ]]; then
            rm -f "$f"
        fi
    done
fi

echo ""
echo "╔══════════════════════════════════════════════════════════╗"
echo "║   Paso 3: Re-empaquetando JAR optimizado...             ║"
echo "╚══════════════════════════════════════════════════════════╝"
cd "$WORK_DIR"
jar cf "$OUTPUT_JAR" .

SIZE=$(du -sh "$OUTPUT_JAR" | cut -f1)
SIZE_BYTES=$(wc -c < "$OUTPUT_JAR")

echo "  → JAR final: $OUTPUT_JAR"
echo "  → Tamaño: $SIZE"

if [ "$SIZE_BYTES" -gt $((5 * 1024 * 1024)) ]; then
    echo ""
    echo "  ⚠  El JAR supera 5 MB (${SIZE}). Directorios más pesados:"
    cd "$WORK_DIR"
    du -sh */ 2>/dev/null | sort -rh | head -10
else
    echo "  ✓  JAR cumple con el límite de 5 MB"
fi

echo ""
echo "╔══════════════════════════════════════════════════════════╗"
echo "║   Paso 4: Generando ZIP del código fuente...            ║"
echo "╚══════════════════════════════════════════════════════════╝"
cd "$PROJECT_DIR"
zip -q -r "$OUTPUT_ZIP" src/ pom.xml proguard.conf build-optimized.sh \
    --exclude "*.class" --exclude "*/target/*" --exclude "*.iml" \
    --exclude "*/.git/*" --exclude "*/.idea/*"

ZIP_SIZE=$(du -sh "$OUTPUT_ZIP" | cut -f1)
ZIP_BYTES=$(wc -c < "$OUTPUT_ZIP")
echo "  → ZIP generado: $OUTPUT_ZIP"
echo "  → Tamaño: $ZIP_SIZE"

echo ""
echo "╔══════════════════════════════════════════════════════════╗"
echo "║   RESUMEN FINAL                                         ║"
echo "╠══════════════════════════════════════════════════════════╣"
printf "║  JAR ejecutable:  %-37s ║\n" "$SIZE"
printf "║  ZIP fuente:      %-37s ║\n" "$ZIP_SIZE"
echo "╠══════════════════════════════════════════════════════════╣"

if [ "$SIZE_BYTES" -le $((5 * 1024 * 1024)) ] && [ "$ZIP_BYTES" -le $((5 * 1024 * 1024)) ]; then
    echo "║  ✓  AMBOS ARCHIVOS cumplen con el límite de 5 MB       ║"
else
    [ "$SIZE_BYTES" -gt $((5 * 1024 * 1024)) ] && echo "║  ✗  El JAR supera 5 MB                                  ║"
    [ "$ZIP_BYTES" -gt $((5 * 1024 * 1024)) ]  && echo "║  ✗  El ZIP supera 5 MB                                  ║"
fi
echo "╚══════════════════════════════════════════════════════════╝"
