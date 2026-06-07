package jeziel.compiladordeensamblador.modelo.parser;

import jeziel.compiladordeensamblador.modelo.lexer.Token;

import java.util.List;

public class AnalizarPseudoinstruccion {
    private List<Token> resultado;
    private ErrorSintactico errorSintactico;

    public AnalizarPseudoinstruccion(Token primerToken){
        resultado.add(primerToken);
    }

    public void recibirSiguienteToken(Token tokenSiguiente){
        //
    }

    //

}
