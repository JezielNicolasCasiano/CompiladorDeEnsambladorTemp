package jeziel.compiladordeensamblador.modelo.semantico;

import jeziel.compiladordeensamblador.modelo.lexer.Token;

public class ErrorSemantico {
    private final Token token;
    private final String mensaje;
    private int posicionArbolAST;

    public ErrorSemantico(Token token, String mensaje,int posicionArbolAST) {
        this.token = token;
        this.mensaje = mensaje;
        this.posicionArbolAST = posicionArbolAST;
    }

    public Token getToken() { return token; }
    public String getMensaje() { return mensaje; }
    public int getPosicionArbolAST() {
        return posicionArbolAST;
    }

    @Override
    public String toString() {
        String lugar = token != null ? " en '" + token.getValue() + "'" : "";
        return "Error semántico" + lugar + ": " + mensaje;
    }
}