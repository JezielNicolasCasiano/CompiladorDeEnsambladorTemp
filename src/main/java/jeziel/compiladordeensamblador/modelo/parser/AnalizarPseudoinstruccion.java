package jeziel.compiladordeensamblador.modelo.parser;

import jeziel.compiladordeensamblador.modelo.lexer.Token;

import java.util.List;

public class AnalizarPseudoinstruccion extends AnalizadorGeneral {

    public AnalizarPseudoinstruccion(Token primerToken, List<Token> lineaAAnalizar) {
        super(primerToken, lineaAAnalizar);
    }


    public void recibirSiguienteToken(Token tokenSiguiente){
        //
    }

    //

}
