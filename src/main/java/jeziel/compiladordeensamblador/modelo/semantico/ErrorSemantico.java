package jeziel.compiladordeensamblador.modelo.semantico;

import jeziel.compiladordeensamblador.modelo.lexer.Token;

public class ErrorSemantico {
    private final Token token;
    private final String mensaje;

    public ErrorSemantico(Token token, String mensaje) {
        this.token = token;
        this.mensaje = mensaje;
    }

    public Token getToken() { return token; }
    public String getMensaje() { return mensaje; }

    @Override
    public String toString() {
        String lugar = token != null ? " en '" + token.getValue() + "'" : "";
        return "Error semántico" + lugar + ": " + mensaje;
    }
}