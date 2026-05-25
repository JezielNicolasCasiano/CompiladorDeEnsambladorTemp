package jeziel.compiladordeensamblador.modelo.parser.nodo;

import jeziel.compiladordeensamblador.modelo.lexer.Token;

public class NodoDosOperandos extends Nodo {
    private Token instruccion;
    private Token destino;
    private Token origen;

    public NodoDosOperandos(Token instruccion, Token destino, Token origen) {
        this.instruccion = instruccion;
        this.destino = destino;
        this.origen = origen;
    }

    public Token getInstruccion() {
        return instruccion;
    }
    public Token getDestino() {
        return destino;
    }
    public Token getOrigen() {
        return origen;
    }
}