package jeziel.compiladordeensamblador.modelo.semantico;

import jeziel.compiladordeensamblador.modelo.lexer.Token;
import jeziel.compiladordeensamblador.modelo.parser.LineaAnalizada;

import java.util.List;

public abstract class AnalizadorSemanticoGeneral {
    private ErrorSemantico errorSemantico;
    private List<Token> lineaAAnalizar;
    private LineaAnalizada lineaAnalizada;
    private Token primerToken;
    private List<LineaAnalizadaSemanticamente> tablaDeSimbolos;

    public AnalizadorSemanticoGeneral(Token primerToken, LineaAnalizada lineaAnalizada, List<LineaAnalizadaSemanticamente> tablaDeSimbolos) {
        this.primerToken = primerToken;
        this.lineaAnalizada = lineaAnalizada;
        this.lineaAAnalizar = lineaAnalizada.getTokens();
        this.tablaDeSimbolos = tablaDeSimbolos;
    }

    public abstract void analizar();

    // Getters and setters
    public ErrorSemantico getErrorSemantico() {
        return errorSemantico;
    }

    public void setErrorSemantico(ErrorSemantico errorSemantico) {
        this.errorSemantico = errorSemantico;
    }

    public List<Token> getLineaAAnalizar() {
        return lineaAAnalizar;
    }

    public Token getPrimerToken() {
        return primerToken;
    }

    public LineaAnalizada getLineaAnalizada() {
        return lineaAnalizada;
    }

    public List<LineaAnalizadaSemanticamente> getTablaDeSimbolos() {
        return tablaDeSimbolos;
    }
}
