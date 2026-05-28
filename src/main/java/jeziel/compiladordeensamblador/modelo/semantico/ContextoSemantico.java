package jeziel.compiladordeensamblador.modelo.semantico;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContextoSemantico {
    private final Map<String, Simbolo> tablaSimbolos = new HashMap<>();
    private final List<ErrorSemantico> errores = new ArrayList<>();
    private int contadorUbicacion = 0;


    public Map<String, Simbolo> getTablaSimbolos() { return tablaSimbolos; }
    public List<ErrorSemantico> getErrores() { return errores; }

    public int getContadorUbicacion() { return contadorUbicacion; }
    public void setContadorUbicacion(int val) { this.contadorUbicacion = val; }
    public void incrementarContador(int bytes) { this.contadorUbicacion += bytes; }

    public void registrarError(ErrorSemantico error) {
        errores.add(error);
    }
}