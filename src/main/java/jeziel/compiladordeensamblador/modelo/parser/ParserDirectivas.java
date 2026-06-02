package jeziel.compiladordeensamblador.modelo.parser;

import java.util.List;
import jeziel.compiladordeensamblador.modelo.lexer.*;

public class ParserDirectivas {

    public static NodoAST parse(Parser p, List<ErrorSintactico> errores) {
        Token dir = p.consumir(TokenType.PSEUDOINSTRUCCION);
        if (dir == null) return null;

        NodoAST nodo = new NodoAST(NodoAST.Tipo.DIRECTIVA, dir);

        TokenSubtype.Directiva subtipo = (TokenSubtype.Directiva) dir.getSub();

        if (subtipo == null) {
            errores.add(new ErrorSintactico(dir, "Pseudoinstruccion no reconocida: '" + dir.getValue() + "'"));
            return nodo;
        }

        switch (subtipo) {
            // DB / DW
            case DB:
            case DW:
                nodo.agregarHijo(p.parseOperando());
                while (p.actual() != null && p.actual().getLinea() == dir.getLinea() &&
                        (p.estipo(TokenType.SEPARADOR) || p.estipo(TokenType.VARIABLE) || p.estipo(TokenType.CONSTANTE) || p.estipo(TokenType.CADENA))) {

                    if (p.estipo(TokenType.SEPARADOR)) {
                        p.consumir();
                    }
                    nodo.agregarHijo(p.parseOperando());
                }
                break;

            // EQU
            case EQU:
                nodo.agregarHijo(p.parseOperando());
                break;

            // ORG
            case ORG:
                nodo.agregarHijo(new NodoAST(NodoAST.Tipo.OPERANDO_CONSTANTE, p.consumir(TokenType.CONSTANTE)));
                break;

            case SEGMENT:
            case ENDS:
            case PROC:
            case ENDP:
            case MACRO:
            case ENDM:
            case END:
            case STACK:
            case DATA:
            case CODE:
            case STACK_SEGMENT:
            case DATA_SEGMENT:
            case CODE_SEGMENT:
                if (p.actual() != null && p.actual().getLinea() == dir.getLinea() && p.estipo(TokenType.VARIABLE)) {
                    nodo.agregarHijo(new NodoAST(NodoAST.Tipo.OPERANDO_VARIABLE, p.consumir()));
                }
                break;

            // DUP
            case DUP:
                p.consumir(TokenType.PARENTESIS_ABRE);
                nodo.agregarHijo(p.parseOperando());
                p.consumir(TokenType.PARENTESIS_CIERRA);
                break;

            default:
                break;
        }

        return nodo;
    }
    }

