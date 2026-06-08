package jeziel.compiladordeensamblador.modelo.parser;

import jeziel.compiladordeensamblador.modelo.lexer.Token;

import java.util.List;

public class AnalizarEtiqueta extends AnalizadorGeneral{

    public AnalizarEtiqueta(Token primerToken, List<Token> lineaAAnalizar) {
        super(primerToken, lineaAAnalizar);
    }
}
