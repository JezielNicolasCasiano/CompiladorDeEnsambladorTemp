package jeziel.compiladordeensamblador.modelo.parser;

import jeziel.compiladordeensamblador.modelo.lexer.Token;

import java.util.List;

public class AnalizarInstruccion extends AnalizadorGeneral{

    public AnalizarInstruccion(Token primerToken, List<Token> lineaAAnalizar) {
        super(primerToken, lineaAAnalizar);
    }
}

