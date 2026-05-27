package jeziel.compiladordeensamblador.modelo.parser;

import java.util.List;
import jeziel.compiladordeensamblador.modelo.lexer.*;

public class ParserInstrucciones {

    public static NodoAST parse(Parser p, List<ErrorSintactico> errores) {
        Token inst = p.consumir(TokenType.INSTRUCCION);
        if (inst == null) return null;

        NodoAST nodo = new NodoAST(NodoAST.Tipo.INSTRUCCION, inst);
        TokenSubtype.Instruccion subtipo = (TokenSubtype.Instruccion) inst.getSub();

        if (subtipo == null) {
            errores.add(new ErrorSintactico(inst, "Instrucción no reconocida: '" + inst.getValue() + "'"));
            return nodo;
        }

        switch (subtipo) {
            // dos operandos: destino, fuente
            case MOV:
            case ADD:
            case LDS:
                nodo.agregarHijo(p.parseOperando());
                p.consumir(TokenType.SEPARADOR);
                nodo.agregarHijo(p.parseOperando());
                break;

            // un operando
            case INC:
            case NEG:
            case DIV:
            case IMUL:
                nodo.agregarHijo(p.parseOperando());
                break;

            // sin operandos
            case CBW:
            case CLC:
	        case LODSB:
            case LODSW:
            case STOSB:
            case STOSW:
                break;

            // saltos — un operando (etiqueta o variable destino)
            case JMP:
            case JNS:
            case JS:
            case JG:
            case JNBE:
            case LOOPNE:
                nodo.agregarHijo(p.parseOperando());
                break;

            // ROR: registro, cantidad
            case ROR:
                nodo.agregarHijo(p.parseOperando());
                p.consumir(TokenType.SEPARADOR);
                nodo.agregarHijo(p.parseOperando());
                break;

            // INT: constante
            /*case INT:
                nodo.agregarHijo(new NodoAST(NodoAST.Tipo.OPERANDO_CONSTANTE, p.consumir(TokenType.CONSTANTE)));
                break;*/

            default:
                errores.add(new ErrorSintactico(inst, "Instrucción sin regla definida: '" + inst.getValue() + "'"));
        }

        return nodo;
    }
}
