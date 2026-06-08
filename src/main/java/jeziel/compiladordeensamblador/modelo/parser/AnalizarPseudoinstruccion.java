package jeziel.compiladordeensamblador.modelo.parser;

import jeziel.compiladordeensamblador.modelo.lexer.Token;

import java.util.List;

public class AnalizarPseudoinstruccion {
    private List<Token> resultado;
    private ErrorSintactico errorSintactico;
    private List<Token> lineaAAnalizar;

    public AnalizarPseudoinstruccion(Token primerToken, List<Token> lineaAAnalizar){
        resultado.add(primerToken);
        this.lineaAAnalizar = lineaAAnalizar;
    }

    public void recibirSiguienteToken(Token tokenSiguiente){
        //
    }

    //

}
