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

        // Validar que el segundo token sea una pseudoinstruccion de tamaño válida
        if (tokenTamano.getType() != TokenType.PSEUDOINSTRUCCION) {
            setErrorSintactico(new ErrorSintactico(tokenTamano));
            getErrorSintactico().setMensajeError("Tamaño invalido");
            return;
        }

        Object subtipo = tokenTamano.getSub();
        if (subtipo != TokenSubtype.Directiva.DB &&
                subtipo != TokenSubtype.Directiva.DW &&
                subtipo != TokenSubtype.Directiva.EQU) {

            setErrorSintactico(new ErrorSintactico(tokenTamano));
            getErrorSintactico().setMensajeError("Tamaño invalida");
            return;
        }

        // Si es la estructura especial: VARIABLE PSEUDOINSTRUCCION CONSTANTE DUP
        // por ejemplo: var1 db 32 dup(0)
        if (getLineaAAnalizar().size() == 4) {
            Token tokenCount = getLineaAAnalizar().get(2);
            Token tokenDup = getLineaAAnalizar().get(3);
            if (tokenCount.getType() == TokenType.CONSTANTE &&
                tokenDup.getType() == TokenType.PSEUDOINSTRUCCION &&
                tokenDup.getSub() == TokenSubtype.Directiva.DUP) {
                return; // Es sintácticamente correcto
            }
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

    private void validarArreglo(int indiceInicio) {
        boolean esperarComa = true;

        for (int i = indiceInicio; i < getLineaAAnalizar().size(); i++) {
            Token actual = getLineaAAnalizar().get(i);

            if (esperarComa) {
                if (actual.getType() != TokenType.SEPARADOR) {
                    setErrorSintactico(new ErrorSintactico(actual));
                    getErrorSintactico().setMensajeError("Se esperaba separador");
                    return;
                }
                esperarComa = false; // El siguiente token debe ser un valor
            } else {
                if (!esValorValido(actual)) {
                    setErrorSintactico(new ErrorSintactico(actual));
                    getErrorSintactico().setMensajeError("Valor de arreglo inváli");
                    return;
                }
                esperarComa = true;
            }
        }

        if (!esperarComa && getLineaAAnalizar().size() > indiceInicio) {
            setErrorSintactico(new ErrorSintactico(getLineaAAnalizar().getLast()));
            getErrorSintactico().setMensajeError("Declaración de arreglo incompleta");
        }
    }
}
