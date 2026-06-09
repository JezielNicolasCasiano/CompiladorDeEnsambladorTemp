package jeziel.compiladordeensamblador.modelo.parser;

import jeziel.compiladordeensamblador.modelo.lexer.Token;
import jeziel.compiladordeensamblador.modelo.lexer.TokenSubtype;
import jeziel.compiladordeensamblador.modelo.lexer.TokenType;

import java.util.List;

public class AnalizarPseudoinstruccion extends AnalizadorGeneral {

    public AnalizarPseudoinstruccion(Token primerToken, List<Token> lineaAAnalizar) {
        super(primerToken, lineaAAnalizar);
    }

    @Override
    public void analizar() {
        List<Token> tokens = getLineaAAnalizar();
        if (tokens == null || tokens.isEmpty()) return;


        //Asegurarnos de que el subtipo corresponde a una getPrimerToken
        if (!(getPrimerToken().getSub() instanceof TokenSubtype.Directiva)) {
            setErrorSintactico(new ErrorSintactico(getPrimerToken()));
            getErrorSintactico().setMensajeError("Subtipo de pseudoinstrucción desconocido.");
            return;
        }

        TokenSubtype.Directiva subtipo = (TokenSubtype.Directiva) getPrimerToken().getSub();

        switch (subtipo) {
            case ORG:
                validarOrg(tokens);
                break;

            case END:
                validarEnd(tokens);
                break;

            case DATA:
            case CODE:
            case STACK:
            case STARTUP:
            case EXIT:
            case STACK_SEGMENT:
            case DATA_SEGMENT:
            case CODE_SEGMENT:
            case ENDS:
                validarSinParametros(tokens);
                break;

            case PROC:
            case ENDP:
            case MACRO:
            case ENDM:
            case SEGMENT:
                validarUnParametroIdentificador(tokens);
                break;

            default:
                break;
        }
    }


    private void validarOrg(List<Token> tokens) {
        //ORG requiere exactamente 1 parámetro y debe ser una constante
        if (tokens.size() < 2) {
            setErrorSintactico(new ErrorSintactico(tokens.get(0)));
            getErrorSintactico().setMensajeError("Faltan operandos para la pseudoinstruccion");
            return;
        }
        if (tokens.size() > 2) {
            setErrorSintactico(new ErrorSintactico(tokens.get(2)));
            getErrorSintactico().setMensajeError("Demasiados operandos para la pseudoinstruccion");
            return;
        }

        Token parametro = tokens.get(1);
        if (parametro.getType() != TokenType.CONSTANTE) {
            setErrorSintactico(new ErrorSintactico(parametro));
            getErrorSintactico().setMensajeError("La pseudoinstruccion requiere una constante");
        }
    }

    private void validarEnd(List<Token> tokens) {
        if (tokens.size() > 2) {
            setErrorSintactico(new ErrorSintactico(tokens.get(2)));
            getErrorSintactico().setMensajeError("Demasiados operandos para la pseudoinstruccion");
            return;
        }
        if (tokens.size() == 2) {
            Token parametro = tokens.get(1);
            if (parametro.getType() != TokenType.VARIABLE && parametro.getType() != TokenType.IDENTIFICADOR) {
                setErrorSintactico(new ErrorSintactico(parametro));
                getErrorSintactico().setMensajeError("Operando invalido");
            }
        }
    }


    private void validarSinParametros(List<Token> tokens) {
        // Pseudoinstruccion de segmento simplificadas van absolutamente solas
        if (tokens.size() > 1) {
            setErrorSintactico(new ErrorSintactico(tokens.get(1)));
            getErrorSintactico().setMensajeError("Demasiados operandos para la pseudoinstruccion");
        }
    }

    private void validarUnParametroIdentificador(List<Token> tokens) {
        // Procedimientos, macros o segmentos completos requieren un nombre
        if (tokens.size() < 2) {
            setErrorSintactico(new ErrorSintactico(tokens.get(0)));
            getErrorSintactico().setMensajeError("Faltan operandos para la pseudoinstruccion");
            return;
        }
        if (tokens.size() > 2) {
            setErrorSintactico(new ErrorSintactico(tokens.get(2)));
            getErrorSintactico().setMensajeError("Demasiados operandos para la pseudoinstruccion");
            return;
        }
        Token parametro = tokens.get(1);
        if (parametro.getType() != TokenType.VARIABLE && parametro.getType() != TokenType.IDENTIFICADOR) {
            setErrorSintactico(new ErrorSintactico(parametro));
            getErrorSintactico().setMensajeError("Operando invalido");
        }
    }
}
