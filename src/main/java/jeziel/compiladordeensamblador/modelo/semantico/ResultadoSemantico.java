package jeziel.compiladordeensamblador.modelo.semantico;

import java.util.List;
import java.util.Map;

public class ResultadoSemantico {
    private final Map<String, Simbolo> tablaSimbolos;
    private final List<ErrorSemantico> errores;

    public ResultadoSemantico(Map<String, Simbolo> tablaSimbolos, List<ErrorSemantico> errores) {
        this.tablaSimbolos = tablaSimbolos;
        this.errores = errores;
    }

    public Map<String, Simbolo> getTablaSimbolos() { return tablaSimbolos; }
    public List<ErrorSemantico> getErrores() { return errores; }
    public boolean tieneErrores() { return !errores.isEmpty(); }
}