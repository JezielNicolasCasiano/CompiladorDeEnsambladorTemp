package jeziel.compiladordeensamblador.modelo.parser;

import jeziel.compiladordeensamblador.modelo.lexer.Token;
import jeziel.compiladordeensamblador.modelo.lexer.TokenSubtype;
import jeziel.compiladordeensamblador.modelo.lexer.TokenType;

public class ErrorSintactico {
    Token tokenErroneo;
    String tokenValue;
    int tokenLinea;
    String tokenType;
    String tokenSubtype;
    String mensajeError;

    public ErrorSintactico(Token tokenErroneo){
        this.tokenErroneo = tokenErroneo;
        this.tokenValue = tokenErroneo.getValue();
        this.tokenLinea = tokenErroneo.getLinea();
        this.tokenType = tokenErroneo.getType().toString();
        this.tokenSubtype = tokenErroneo.getSub().toString();

    }

    public void obtenerMensajeError(){

    }

}
