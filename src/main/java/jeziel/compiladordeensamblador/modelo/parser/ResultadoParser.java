package compilador8086;

import java.util.List;

public class ResultadoParser {

    private final List<NodoAST> arbol;
    private final List<ErrorSintactico> errores;

    public ResultadoParser(List<NodoAST> arbol, List<ErrorSintactico> errores) {
        this.arbol = arbol;
        this.errores = errores;
    }

    public List<NodoAST> getArbol()          { return arbol; }
    public List<ErrorSintactico> getErrores() { return errores; }
    public boolean tieneErrores()             { return !errores.isEmpty(); }
}
