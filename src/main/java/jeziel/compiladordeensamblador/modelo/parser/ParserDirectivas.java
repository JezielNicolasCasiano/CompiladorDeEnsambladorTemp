package jeziel.compiladordeensamblador.modelo.parser;

import jeziel.compiladordeensamblador.modelo.lexer.*;

import java.util.List;

public class ParserDirectivas {

    public static NodoAST parse(Parser p, List<ErrorSintactico> errores) {
        Token dir = p.consumir(TokenType.PSEUDOINSTRUCCION);
        if (dir == null) return null;

        NodoAST nodo = new NodoAST(NodoAST.Tipo.DIRECTIVA, dir);
        String val = dir.getValue().toUpperCase().replace(" ", "_");
        TokenSubtype.Directiva subtipo;

        try {
            subtipo = TokenSubtype.Directiva.valueOf(val);
        } catch (IllegalArgumentException e) {
            p.lanzarError(dir, "Pseudoinstruccion no reconocida: '" + dir.getValue() + "'");
            return nodo;
        }

        switch (subtipo) {
            case DB:
            case DW:
                nodo.agregarHijo(p.parseOperando());
                while (p.estipo(TokenType.SEPARADOR)) {
                    p.consumir(TokenType.SEPARADOR);
                    nodo.agregarHijo(p.parseOperando());
                }
                break;

            case EQU:
                nodo.agregarHijo(p.parseOperando());
                break;

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
                if (p.estipo(TokenType.VARIABLE)) {
                    nodo.agregarHijo(new NodoAST(NodoAST.Tipo.OPERANDO_VARIABLE, p.consumir()));
                }
                break;

            default:
                break;
        }

        return nodo;
    }
}
