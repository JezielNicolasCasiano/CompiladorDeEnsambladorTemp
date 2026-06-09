package jeziel.compiladordeensamblador.modelo.parser;

import jeziel.compiladordeensamblador.modelo.lexer.Token;

import java.util.List;

public class AnalizarEtiqueta extends AnalizadorGeneral{


    public AnalizarEtiqueta(Token primerToken, List<Token> lineaAAnalizar) {
        super(primerToken, lineaAAnalizar);
    }

    @Override
    public void analizar() {
        if (getLineaAAnalizar().size() > 1) {
            Token tokenInesperado = getLineaAAnalizar().get(1);
            setErrorSintactico(new ErrorSintactico(tokenInesperado));
            getErrorSintactico().setMensajeError("Instrucciones o pseudo instrucciones sobrantes");
        }
    }
}
