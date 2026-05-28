package jeziel.compiladordeensamblador.modelo.parser;

import java.util.List;
import jeziel.compiladordeensamblador.modelo.lexer.*;

public class ParserOperandos {

    public static NodoAST parse(Parser p, List<ErrorSintactico> errores) {

        if (p.estipo(TokenType.REGISTRO)) {
            return new NodoAST(NodoAST.Tipo.OPERANDO_REGISTRO, p.consumir());
        }

        if (p.estipo(TokenType.CONSTANTE)) {
            return new NodoAST(NodoAST.Tipo.OPERANDO_CONSTANTE, p.consumir());
        }

        if (p.estipo(TokenType.CARACTER)) {
            return new NodoAST(NodoAST.Tipo.OPERANDO_CARACTER, p.consumir());
        }

        if (p.estipo(TokenType.VARIABLE)) {
            return new NodoAST(NodoAST.Tipo.OPERANDO_VARIABLE, p.consumir());
        }

        if (p.estipo(TokenType.CORCHETE_ABRE)) {
            return parseMemoria(p, errores);
        }
        if (p.estipo(TokenType.PSEUDOINSTRUCCION) && p.esSubtipo(TokenSubtype.Directiva.OFFSET)) {
            Token tokenOffset = p.consumir(); // Consumimos la palabra OFFSET
            NodoAST nodoOffset = new NodoAST(NodoAST.Tipo.OPERANDO_OFFSET, tokenOffset);

            // Inmediatamente después debe venir una variable
            if (p.estipo(TokenType.VARIABLE)) {
                nodoOffset.agregarHijo(new NodoAST(NodoAST.Tipo.OPERANDO_VARIABLE, p.consumir()));
            } else {
                errores.add(new ErrorSintactico(p.getActual(), "Se esperaba una variable después de OFFSET"));
            }
            return nodoOffset;
        }

        Token t = p.getActual();
        errores.add(new ErrorSintactico(t,
            "Se esperaba un operando pero se encontró " +
            (t != null ? "'" + t.getValue() + "'" : "fin de archivo")));
        if (t != null) p.consumir();
        return null;
    }

    private static NodoAST parseMemoria(Parser p, List<ErrorSintactico> errores) {
        p.consumir(TokenType.CORCHETE_ABRE);
        NodoAST nodo = new NodoAST(NodoAST.Tipo.OPERANDO_MEMORIA, null);

        if (p.estipo(TokenType.REGISTRO)) {
            nodo.agregarHijo(new NodoAST(NodoAST.Tipo.OPERANDO_REGISTRO, p.consumir()));
        } else if (p.estipo(TokenType.VARIABLE)) {
            nodo.agregarHijo(new NodoAST(NodoAST.Tipo.OPERANDO_VARIABLE, p.consumir()));
        } else {
            errores.add(new ErrorSintactico(p.getActual(), "Se esperaba registro o variable dentro de []"));
        }

        p.consumir(TokenType.CORCHETE_CIERRA);
        return nodo;
    }
}
