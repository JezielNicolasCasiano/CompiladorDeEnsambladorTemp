package compilador8086;

public class ErrorSintactico {

    private final Token token;
    private final String mensaje;

    public ErrorSintactico(Token token, String mensaje) {
        this.token = token;
        this.mensaje = mensaje;
    }

    public Token getToken()    { return token; }
    public String getMensaje() { return mensaje; }

    @Override
    public String toString() {
        String lugar = token != null ? " en '" + token.getValue() + "'" : "";
        return "Error sintáctico" + lugar + ": " + mensaje;
    }
}
