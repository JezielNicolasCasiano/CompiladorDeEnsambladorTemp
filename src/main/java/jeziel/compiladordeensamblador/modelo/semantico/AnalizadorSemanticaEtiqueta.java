package jeziel.compiladordeensamblador.modelo.semantico;

import jeziel.compiladordeensamblador.modelo.lexer.Token;
import jeziel.compiladordeensamblador.modelo.parser.LineaAnalizada;

import java.util.List;

public class AnalizadorSemanticaEtiqueta extends AnalizadorSemanticoGeneral {

    public AnalizadorSemanticaEtiqueta(Token primerToken, LineaAnalizada lineaAnalizada, List<LineaAnalizadaSemanticamente> tablaDeSimbolos) {
        super(primerToken, lineaAnalizada, tablaDeSimbolos);
    }

    @Override
    public void analizar() {
        String nombre = getPrimerToken().getValue();
        // Quitar dos puntos al final si existen
        if (nombre.endsWith(":")) {
            nombre = nombre.substring(0, nombre.length() - 1);
        }

        // Validar que el símbolo no esté duplicado
        for (LineaAnalizadaSemanticamente sym : getTablaDeSimbolos()) {
            if (sym.getLineaAnalizada() != getLineaAnalizada()) {
                Token symToken = sym.getLineaAnalizada().getTokens().getFirst();
                String symNombre = symToken.getValue();
                if (symNombre.endsWith(":")) {
                    symNombre = symNombre.substring(0, symNombre.length() - 1);
                }
                if (symNombre.equalsIgnoreCase(nombre)) {
                    ErrorSemantico error = new ErrorSemantico(getPrimerToken());
                    error.setMensajeError("Símbolo duplicado (etiqueta): " + nombre);
                    setErrorSemantico(error);
                    return;
                }
            }
        }
    }
}
