package jeziel.compiladordeensamblador.modelo.parser.nodo;

import jeziel.compiladordeensamblador.modelo.lexer.Token;


public class NodoUnOperando extends Nodo {
    private Token instruccion;
    private Token operando;

    public NodoUnOperando(Token instruccion, Token operando) {
        this.instruccion = instruccion;
        this.operando = operando;
    }

    public Token getInstruccion() {
        return instruccion;
    }
    public Token getOperando() {
        return operando;
    }
}