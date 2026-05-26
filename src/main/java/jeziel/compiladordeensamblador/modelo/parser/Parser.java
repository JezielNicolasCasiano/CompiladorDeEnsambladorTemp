package jeziel.compiladordeensamblador.modelo.parser;

import jeziel.compiladordeensamblador.modelo.lexer.*;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    private final List<Token> tokens;
    private int pos;
    private final List<ErrorSintactico> errores;
    private final List<NodoAST> arbol;
    private EstadoPrograma estadoActual = EstadoPrograma.INICIO;

    public enum EstadoPrograma {
        INICIO, STACK, DATA, CODE
    }


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



    private boolean check(TokenType tipo) {
        return actual() != null && actual().getType() == tipo;
    }

    private boolean checkValor(String valor) {
        return actual() != null && actual().getValue().equalsIgnoreCase(valor);
    }

    private boolean checkSubtipo(Enum<?> subtipo) {
        return actual() != null && subtipo.equals(actual().getSub());
    }

    private Token consume(TokenType tipo) {
        Token t = actual();
        if (t == null || t.getType() != tipo) {
            String encontrado = (t != null) ? t.getType() + " ('" + t.getValue() + "')" : "fin de archivo";

            lanzarError(t, "Se esperaba " + tipo + " pero se encontró " + encontrado);
        }
        pos++;
        return t;
    }
    private NodoAST parseLinea() {
        if (actual() == null) return null;

        if (check(TokenType.PSEUDOINSTRUCCION)) {
            Token t = actual();
            Enum<?> sub = t.getSub();

            if (sub == TokenSubtype.Directiva.STACK || sub == TokenSubtype.Directiva.STACK_SEGMENT) {
                estadoActual = EstadoPrograma.STACK;
            } else if (sub == TokenSubtype.Directiva.DATA || sub == TokenSubtype.Directiva.DATA_SEGMENT) {
                estadoActual = EstadoPrograma.DATA;
            } else if (sub == TokenSubtype.Directiva.CODE || sub == TokenSubtype.Directiva.CODE_SEGMENT) {
                estadoActual = EstadoPrograma.CODE;
            }
            return parseDirectiva();
        }

        if (check(TokenType.DESCONOCIDO)) {
            lanzarError(actual(), "Token desconocido: '" + actual().getValue() + "'");
        }

        if (check(TokenType.PSEUDOINSTRUCCION)) {
            Token t = actual();
            Enum<?> sub = t.getSub();

            if (sub == TokenSubtype.Directiva.STACK || sub == TokenSubtype.Directiva.STACK_SEGMENT) {
                estadoActual = EstadoPrograma.STACK;
            }
            else if (sub == TokenSubtype.Directiva.DATA || sub == TokenSubtype.Directiva.DATA_SEGMENT) {
                estadoActual = EstadoPrograma.DATA;
            }
            else if (sub == TokenSubtype.Directiva.CODE || sub == TokenSubtype.Directiva.CODE_SEGMENT) {
                estadoActual = EstadoPrograma.CODE;
            }
            return parseDirectiva();
        }

        if (check(TokenType.INSTRUCCION)) {
            if (estadoActual != EstadoPrograma.CODE) {
                lanzarError(actual(), "Instrucción fuera de lugar: Debe estar dentro de un segmento .code");
            }
            return parseInstruccion();
        }

        if (check(TokenType.VARIABLE)) {
            if (estadoActual != EstadoPrograma.DATA && estadoActual != EstadoPrograma.STACK) {
                lanzarError(actual(), "Declaración fuera de lugar: Las variables deben estar en .data o .stack");
            }
            return parseDeclaracionVariable();
        }

        if (check(TokenType.ETIQUETA)) return parseEtiqueta();

        Token t = consume();
        lanzarError(t, "Token inesperado al inicio de línea (posiblemente fuera de segmento): '" + t.getValue() + "'");
        return null;
    }

    private void sincronizar() {
        if (actual() != null) {
            consume();
        }

        while (actual() != null) {
            TokenType tipo = actual().getType();
            if (tipo == TokenType.ETIQUETA ||
                    tipo == TokenType.INSTRUCCION ||
                    tipo == TokenType.PSEUDOINSTRUCCION ||
                    tipo == TokenType.VARIABLE) {
                return;
            }

            Token omitido = consume();
            registrarError(new ErrorSintactico(omitido, "Token omitido por error de sintaxis previo"));
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



    private NodoAST parseDeclaracionVariable() {
        Token tokenVariable = consume(TokenType.VARIABLE);
        NodoAST nodoVariable = new NodoAST(NodoAST.Tipo.OPERANDO_VARIABLE, tokenVariable);

        if (check(TokenType.PSEUDOINSTRUCCION)) {
            NodoAST nodoDirectiva = parseDirectiva();
            if (nodoDirectiva != null) {
                nodoDirectiva.getHijos().add(0, nodoVariable);
                return nodoDirectiva;
            }
        }
        lanzarError(getActual(), "Se esperaba pseduinstruccion (DB, DW, EQU) después de la variable '" + tokenVariable.getValue() + "'");
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

    public void lanzarError(Token t, String mensaje) {
        throw new ErrorSintacticoException(new ErrorSintactico(t, mensaje));
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
