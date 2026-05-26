package jeziel.compiladordeensamblador.modelo.parser;

import jeziel.compiladordeensamblador.modelo.lexer.*;

import java.util.List;

public class ParserOperandos {

    public static NodoAST parse(Parser p, List<ErrorSintactico> errores) {

        if (p.estipo(TokenType.REGISTRO)) {
            return new NodoAST(NodoAST.Tipo.OPERANDO_REGISTRO, p.consumir());
        }

        if (p.estipo(TokenType.CONSTANTE)) {//parche para dup
            Token numToken = p.consumir();

            if (p.getActual() != null && p.estipo(TokenType.PSEUDOINSTRUCCION) && p.getActual().getSub() == TokenSubtype.Directiva.DUP) {
                Token dupToken = p.consumir();
                NodoAST nodoDup = new NodoAST(NodoAST.Tipo.OPERANDO_DUP, dupToken);
                nodoDup.agregarHijo(new NodoAST(NodoAST.Tipo.OPERANDO_CONSTANTE, numToken));
                String dupStr = dupToken.getValue();
                int start = dupStr.indexOf('(') + 1;
                int end = dupStr.lastIndexOf(')');
                String valorInterno = (start > 0 && end > start) ? dupStr.substring(start, end).trim() : "?";
                Token tokenInterno = new Token(TokenType.CONSTANTE, valorInterno);
                nodoDup.agregarHijo(new NodoAST(NodoAST.Tipo.OPERANDO_CONSTANTE, tokenInterno));

                return nodoDup;
            }
            return new NodoAST(NodoAST.Tipo.OPERANDO_CONSTANTE, numToken);
        }

        if (p.estipo(TokenType.CARACTER)) {
            return new NodoAST(NodoAST.Tipo.OPERANDO_CARACTER, p.consumir());
        }

        if (p.estipo(TokenType.VARIABLE)) {
            return new NodoAST(NodoAST.Tipo.OPERANDO_VARIABLE, p.consumir());
        }

        if (p.estipo(TokenType.CADENA)) {
            return new NodoAST(NodoAST.Tipo.OPERANDO_CADENA, p.consumir());
        }

        if (p.estipo(TokenType.CORCHETE_ABRE)) {
            return parseMemoria(p, errores);
        }

        Token t = p.getActual();
        String valorToken = (t != null) ? "'" + t.getValue() + "'" : "fin de archivo";
        p.lanzarError(t, "Se esperaba un operando pero se encontró " + valorToken);
        return null;
    }

    private static NodoAST parseMemoria(Parser p, List<ErrorSintactico> errores) {
        p.consumir(TokenType.CORCHETE_ABRE);
        NodoAST nodo = new NodoAST(NodoAST.Tipo.OPERANDO_MEMORIA, null);

        if (p.estipo(TokenType.REGISTRO)) {
            nodo.agregarHijo(new NodoAST(NodoAST.Tipo.OPERANDO_REGISTRO, p.consumir()));
        } else if (p.estipo(TokenType.VARIABLE)) {
            nodo.agregarHijo(new NodoAST(NodoAST.Tipo.OPERANDO_VARIABLE, p.consumir()));


        }


        else {
            p.lanzarError(p.getActual(), "Se esperaba registro o variable dentro de []");
        }

        p.consumir(TokenType.CORCHETE_CIERRA);
        return nodo;
    }
}
