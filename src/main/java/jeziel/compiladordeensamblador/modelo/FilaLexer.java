package jeziel.compiladordeensamblador.modelo;

import jeziel.compiladordeensamblador.modelo.lexer.Token;
import jeziel.compiladordeensamblador.modelo.lexer.TokenType;

import java.util.Map;

public class FilaLexer {

    private final int numero;
    private final String separacion;
    private final String token;

    public FilaLexer(int numero, String separacion, String token) {
        this.numero = numero;
        this.separacion = separacion;
        this.token = token;
    }

    public int getNumero(){
        return numero;
    }

    public String getSeparacion() {
        return separacion;
    }

    public String getToken() {
        return token;
    }

    public static FilaLexer crearDesdeToken(int indice, Token token, Map<TokenType, String> descripciones) {
        String desc;
        if (token.getType() == TokenType.CONSTANTE) {
            desc = "Constante (numérica " + String.valueOf(token.getSub()).toLowerCase() + ")";
        } else {
            desc = descripciones.getOrDefault(token.getType(), "Elemento inválido");
        }
        indice = indice + 1;
        return new FilaLexer(indice, token.getValue(), desc);
    }

}