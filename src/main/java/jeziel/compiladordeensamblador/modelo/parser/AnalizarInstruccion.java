package jeziel.compiladordeensamblador.modelo.parser;

import jeziel.compiladordeensamblador.modelo.lexer.Token;
import jeziel.compiladordeensamblador.modelo.lexer.TokenSubtype;
import jeziel.compiladordeensamblador.modelo.lexer.TokenType;

import java.util.List;

import static jeziel.compiladordeensamblador.modelo.lexer.TokenSubtype.Instruccion.*;

public class AnalizarInstruccion extends AnalizadorGeneral{

    public AnalizarInstruccion(Token primerToken, List<Token> lineaAAnalizar) {
        super(primerToken, lineaAAnalizar);
    }

    @Override
    public void analizar() {
        List<Token> tokens = getLineaAAnalizar();
        if (tokens == null || tokens.isEmpty()) return;

        // Asegurarnos de que el subtipo corresponde a una Instrucción
        if (!(getPrimerToken().getSub() instanceof TokenSubtype.Instruccion)) {
            setErrorSintactico(new ErrorSintactico(getPrimerToken()));
            getErrorSintactico().setMensajeError("Subtipo de instrucción desconocido.");
            return;
        }

        TokenSubtype.Instruccion subtipo = (TokenSubtype.Instruccion) getPrimerToken().getSub();

        switch (subtipo) {
            // Instrucciones sin operandos
            case CBW:
            case CLC:
            case LODSB:
            case LODSW:
            case STOSB:
            case STOSW:
                validarSinOperandos(tokens);
                break;

            // Instrucciones con un operando
            case DIV:
            case IMUL:
            case INC:
            case NEG:
            case INT:
            case JNS:
            case JS:
            case LOOPNE:
            case JG:
            case JMP:
            case JNBE:
                validarUnOperando(tokens);
                break;

            // Instrucciones con dos operandos
            case ADD:
            case LDS:
            case MOV:
            case ROR:
                validarDosOperandos(tokens);
                break;

            default:
                setErrorSintactico(new ErrorSintactico(getPrimerToken()));
                getErrorSintactico().setMensajeError("Instrucción no soportada");
                break;
        }
    }

    private void validarSinOperandos(List<Token> tokens) {
        if (tokens.size() > 1) {
            setErrorSintactico(new ErrorSintactico(tokens.get(1)));
            getErrorSintactico().setMensajeError("Demasiados operandos para esta instrucción.");
        }
    }

    private void validarUnOperando(List<Token> tokens) {
        if (tokens.size() < 2) {
            setErrorSintactico(new ErrorSintactico(tokens.get(0)));
            getErrorSintactico().setMensajeError("Faltan operandos para la instrucción.");
            return;
        }
        if (tokens.size() > 2) {
            setErrorSintactico(new ErrorSintactico(tokens.get(2)));
            getErrorSintactico().setMensajeError("Demasiados operandos para esta instrucción.");
            return;
        }

        Token operando = tokens.get(1);
        if (!esOperandoValido(operando)) {
            setErrorSintactico(new ErrorSintactico(operando));
            getErrorSintactico().setMensajeError("Operando inválido para la instrucción.");
        }
    }

    private void validarDosOperandos(List<Token> tokens) {
        if (tokens.size() < 4) {
            setErrorSintactico(new ErrorSintactico(tokens.getLast()));
            getErrorSintactico().setMensajeError("Faltan operandos para la instrucción.");
            return;
        }

        Token operando1 = tokens.get(1);
        Token coma = tokens.get(2);
        Token operando2 = tokens.get(3);

        if (!esOperandoValido(operando1)) {
            setErrorSintactico(new ErrorSintactico(operando1));
            getErrorSintactico().setMensajeError("Operando inválido.");
            return;
        }

        if (coma.getType() != TokenType.SEPARADOR) {
            setErrorSintactico(new ErrorSintactico(coma));
            getErrorSintactico().setMensajeError("Se esperaba una coma ',' separando los operandos.");
            return;
        }

        if (!esOperandoValido(operando2)) {
            setErrorSintactico(new ErrorSintactico(operando2));
            getErrorSintactico().setMensajeError("Operando inválido.");
            return;
        }

        if (tokens.size() > 4) {
            setErrorSintactico(new ErrorSintactico(tokens.get(4)));
            getErrorSintactico().setMensajeError("Demasiados operandos para esta instrucción.");
        }
    }

    // Método auxiliar para validar los operandos
    private boolean esOperandoValido(Token token) {
        TokenType t = token.getType();
        return t == TokenType.REGISTRO ||
                t == TokenType.CONSTANTE ||
                t == TokenType.VARIABLE ||
                t == TokenType.IDENTIFICADOR ||
                t == TokenType.COMPUESTO ||
                t == TokenType.ETIQUETA;    }
}

