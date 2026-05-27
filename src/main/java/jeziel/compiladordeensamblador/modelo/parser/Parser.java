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
            try {
                NodoAST nodo = parseLinea();
                if (nodo != null) arbol.add(nodo);
            } catch (ErrorSintacticoException e) {
                errores.add(e.getError());
                sincronizar();
            }
        }
        return new ResultadoParser(arbol, errores);
    }

    private NodoAST parseLinea() {
        if (actual() == null) return null;

        if (check(TokenType.DESCONOCIDO)) {
            Token t = consume();
            errores.add(new ErrorSintactico(t, "Token desconocido: '" + t.getValue() + "'"));
            return null;
        }

        if (check(TokenType.ETIQUETA)) {
            return parseEtiqueta();
        }

        if (check(TokenType.INSTRUCCION)) {
            return parseInstruccion();
        }

        if (check(TokenType.PSEUDOINSTRUCCION)) {
            return parseDirectiva();
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
