package jeziel.compiladordeensamblador.modelo.parser;

import java.util.ArrayList;
import java.util.List;
import jeziel.compiladordeensamblador.modelo.lexer.*;

public class Parser {

    private final List<Token> tokens;
    private int pos;
    private final List<ErrorSintactico> errores;
    private final List<NodoAST> arbol;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.pos = 0;
            this.errores = new ArrayList<>();
        this.arbol = new ArrayList<>();
    }


    private Token actual() {
        if (pos < tokens.size()) return tokens.get(pos);
        return null;
    }

    private Token siguiente() {
        if (pos + 1 < tokens.size()) return tokens.get(pos + 1);
        return null;
    }

    private Token consume() {
        Token t = actual();
        pos++;
        return t;
    }

    private Token consume(TokenType tipo) {
        Token t = actual();
        if (t == null || t.getType() != tipo) {
            errores.add(new ErrorSintactico(
                t,
                "Se esperaba " + tipo + " pero se encontró " + (t != null ? t.getType() + " ('" + t.getValue() + "')" : "fin de archivo")
            ));
            return null;
        }
        pos++;
        return t;
    }

    private boolean check(TokenType tipo) {
        return actual() != null && actual().getType() == tipo;
    }

    private boolean checkValor(String valor) {
        return actual() != null && actual().getValue().equalsIgnoreCase(valor);
    }

    private boolean checkSubtipo(Enum<?> subtipo) {
        return actual() != null && subtipo.equals(actual().getSub());
    }

    private void sincronizar() {
        while (actual() != null
                && actual().getType() != TokenType.ETIQUETA
                && actual().getType() != TokenType.INSTRUCCION
                && actual().getType() != TokenType.PSEUDOINSTRUCCION) {
            consume();
        }
    }

    public ResultadoParser parsear() {
        while (actual() != null) {
            int lineaActual = actual().getLinea();
            try {
                NodoAST nodo = parseLinea();
                boolean hayBasura = false;
                Token primerTokenBasura = null;
                while (actual() != null && actual().getLinea() == lineaActual) {
                    if (!hayBasura) {
                        hayBasura = true;
                        primerTokenBasura = actual();
                    }

                    Token tokenSobrante = consume();

                    if (nodo != null) {
                        nodo.agregarHijo(new NodoAST(NodoAST.Tipo.ERROR_SINTACTICO, tokenSobrante));
                    }
                }
                if (hayBasura) {
                    errores.add(new ErrorSintactico(primerTokenBasura,
                            "Elementos inesperados al final de la línea"));
                }

                if (nodo != null) arbol.add(nodo);
            } catch (ErrorSintacticoException e) {
                errores.add(e.getError());
                arbol.add(new NodoAST(NodoAST.Tipo.ERROR_SINTACTICO, e.getError().getToken()));
                sincronizarRenglon(lineaActual);
            }
        }
        return new ResultadoParser(arbol, errores);
    }

    private NodoAST parseLinea() {
        if (actual() == null) return null;

        if (check(TokenType.DESCONOCIDO)) {
            Token t = consume();
            errores.add(new ErrorSintactico(t, "Token desconocido: '" + t.getValue() + "'"));
            //En vez de mandar null manda el nuevo tipo de error
            return new NodoAST(NodoAST.Tipo.ERROR_LEXICO,t);
        }

        if (check(TokenType.ETIQUETA)) {
            return parseEtiqueta();
        }

        if (check(TokenType.INSTRUCCION)) {
            return parseInstruccion();
        }

        if (check(TokenType.PSEUDOINSTRUCCION) || check(TokenType.VARIABLE)) {
            return parseDirectivaConVariable();
        }

        Token t = consume();
        errores.add(new ErrorSintactico(t, "Token inesperado: '" + t.getValue() + "'"));
        return null;
    }


    private NodoAST parseEtiqueta() {
        Token etiqueta = consume(TokenType.ETIQUETA);
        NodoAST nodoEtiqueta = new NodoAST(NodoAST.Tipo.ETIQUETA, etiqueta);

        if (check(TokenType.INSTRUCCION)) {
            nodoEtiqueta.agregarHijo(parseInstruccion());
        } else if (check(TokenType.PSEUDOINSTRUCCION)) {
            nodoEtiqueta.agregarHijo(parseDirectiva());
        }

        return nodoEtiqueta;
    }

    private NodoAST parseInstruccion() {
        return ParserInstrucciones.parse(this, errores);
    }

    private NodoAST parseDirectiva() {
        return ParserDirectivas.parse(this, errores);
    }

    private NodoAST parseDirectivaConVariable() {
        Token tokenVariable = null;

        if (check(TokenType.VARIABLE)) {
            tokenVariable = consume(TokenType.VARIABLE);
        }
        NodoAST nodoDirectiva = parseDirectiva();
        if (tokenVariable != null && nodoDirectiva != null) {
            nodoDirectiva.agregarHijo(new NodoAST(NodoAST.Tipo.OPERANDO_VARIABLE, tokenVariable));
        }

        return nodoDirectiva;
    }

    private void sincronizarRenglon(int lineaActual) {
        while (actual() != null && actual().getLinea() == lineaActual) {
            consume();
        }
    }

    NodoAST parseOperando() {
        return ParserOperandos.parse(this, errores);
    }

    Token getActual()          { return actual(); }
    Token getSiguiente()       { return siguiente(); }
    Token consumir()           { return consume(); }
    Token consumir(TokenType t){ return consume(t); }
    boolean estipo(TokenType t){ return check(t); }
    boolean esValor(String v)  { return checkValor(v); }
    boolean esSubtipo(Enum<?> s){ return checkSubtipo(s); }
    void registrarError(ErrorSintactico e) { errores.add(e); }
}
