package jeziel.compiladordeensamblador.modelo.semantico;

import jeziel.compiladordeensamblador.modelo.lexer.Token;
import jeziel.compiladordeensamblador.modelo.lexer.TokenSubtype;
import jeziel.compiladordeensamblador.modelo.lexer.TokenType;

public class ErrorSemantico {
    Token tokenErroneo;
    String tokenValue;
    int tokenLinea;
    String tokenType;
    String tokenSubtype;
    String mensajeError;

    public ErrorSemantico(Token tokenErroneo){
        this.tokenErroneo = tokenErroneo;
        this.tokenValue = tokenErroneo.getValue();
        this.tokenLinea = tokenErroneo.getLinea();
        this.tokenType = tokenErroneo.getType().toString();
        this.tokenSubtype = tokenErroneo.getSub() != null ? tokenErroneo.getSub().toString() : "null";

    }

    public void obtenerMensajeError(){

    }




    //getters y setters


    public Token getTokenErroneo() {
        return tokenErroneo;
    }

    public void setTokenErroneo(Token tokenErroneo) {
        this.tokenErroneo = tokenErroneo;
    }

    public String getTokenValue() {
        return tokenValue;
    }

    public void setTokenValue(String tokenValue) {
        this.tokenValue = tokenValue;
    }

    public int getTokenLinea() {
        return tokenLinea;
    }

    public void setTokenLinea(int tokenLinea) {
        this.tokenLinea = tokenLinea;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getTokenSubtype() {
        return tokenSubtype;
    }

    public void setTokenSubtype(String tokenSubtype) {
        this.tokenSubtype = tokenSubtype;
    }

    public String getMensajeError() {
        return mensajeError;
    }

    public void setMensajeError(String mensajeError) {
        this.mensajeError = mensajeError;
    }
}