package jeziel.compiladordeensamblador.modelo.parser;

import jeziel.compiladordeensamblador.modelo.lexer.Token;

import java.util.List;

public class LineaAnalizada {

    private final int numeroLinea;
    private final List<Token> tokens;
    private ErrorSintactico errorSintactico; // Será null si la línea es correcta

    public LineaAnalizada(int lineaAnalizada, List<Token> tokensDeLaLinea) {
        this.numeroLinea = lineaAnalizada;
        this.tokens = tokensDeLaLinea;
        this.errorSintactico = null;
    }

    //getters y setter para errorSintactico
    public int getNumeroLinea() { return numeroLinea; }
    public List<Token> getTokens() { return tokens; }
    public ErrorSintactico getErrorSintactico() { return errorSintactico; }
    public void setErrorSintactico(ErrorSintactico error) { this.errorSintactico = error; }
    public boolean tieneError() {
        return this.errorSintactico != null;
    }
}

