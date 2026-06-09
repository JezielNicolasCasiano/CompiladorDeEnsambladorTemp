package jeziel.compiladordeensamblador.modelo.parser;

import jeziel.compiladordeensamblador.modelo.lexer.Token;
import jeziel.compiladordeensamblador.modelo.lexer.TokenSubtype;
import jeziel.compiladordeensamblador.modelo.lexer.TokenType;

import java.util.List;

public class AnalizarVariable extends AnalizadorGeneral{

    public AnalizarVariable(Token primerToken, List<Token> lineaAAnalizar) {
        super(primerToken, lineaAAnalizar);
    }

    @Override
    public void analizar() {
        if (getLineaAAnalizar().size() < 3) {
            setErrorSintactico(new ErrorSintactico(getLineaAAnalizar().getLast()));
            getErrorSintactico().setMensajeError("Declaración incompleta");
            return;
        }

        Token tokenNombre = getLineaAAnalizar().get(0);
        Token tokenTamano = getLineaAAnalizar().get(1);
        Token tokenValor = getLineaAAnalizar().get(2);

        // Validar que el segundo token sea una directiva de tamaño válida
        if (tokenTamano.getType() != TokenType.PSEUDOINSTRUCCION) {
            setErrorSintactico(new ErrorSintactico(tokenTamano));
            getErrorSintactico().setMensajeError("Se esperaba una pseudoinstruccion de tamaño (DB, DW, EQU)");
            return;
        }

        Object subtipo = tokenTamano.getSub();
        if (subtipo != TokenSubtype.Directiva.DB &&
                subtipo != TokenSubtype.Directiva.DW &&
                subtipo != TokenSubtype.Directiva.EQU) {

            setErrorSintactico(new ErrorSintactico(tokenTamano));
            getErrorSintactico().setMensajeError("Pseudoinstruccion de tamaño invalida");
            return;
        }

        //Validar que el valor inicial sea de un tipo permitido
        if (!esValorValido(tokenValor)) {
            setErrorSintactico(new ErrorSintactico(tokenValor));
            getErrorSintactico().setMensajeError(("Valor asignado inválido"));
            return;
        }

        // Validar si es un arreglo separado por comas
        if (getLineaAAnalizar().size() > 3) {
            validarArreglo(3);
        }
    }

    // Método auxiliar para decidir qué tokens pueden ser valores de una variable
    private boolean esValorValido(Token token) {
        TokenType t = token.getType();
        return t == TokenType.CONSTANTE ||
                t == TokenType.CADENA ||
                t == TokenType.CARACTER ||
                (t == TokenType.PSEUDOINSTRUCCION && token.getSub() == TokenSubtype.Directiva.DUP);
    }

    // Método auxiliar para validar arreglos de datos
    private void validarArreglo(int indiceInicio) {
        for (int i = indiceInicio; i < getLineaAAnalizar().size(); i++) {
            Token actual = getLineaAAnalizar().get(i);

            if (i % 2 != 0) {
                if (actual.getType() != TokenType.SEPARADOR) {
                    setErrorSintactico(new ErrorSintactico(actual));
                    getErrorSintactico().setMensajeError("Se esperaba separador");
                    return;
                }
            }
            else {
                if (!esValorValido(actual)) {
                    setErrorSintactico(new ErrorSintactico(actual));
                    getErrorSintactico().setMensajeError("Valor de arreglo inválido.");
                    return;
                }
            }
        }
    }
}
