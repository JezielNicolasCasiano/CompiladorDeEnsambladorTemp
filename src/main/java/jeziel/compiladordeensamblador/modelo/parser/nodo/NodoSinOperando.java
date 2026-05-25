package jeziel.compiladordeensamblador.modelo.parser.nodo;

import jeziel.compiladordeensamblador.modelo.lexer.Token;

public class NodoSinOperando extends Nodo {
    private Token etiqueta;   // ej. VARIABLE1
    private Token tamano;     // ej. DB o DW
    private Token valor;      // ej. 10 o 'A'

    public NodoSinOperando(Token etiqueta, Token tamano, Token valor) {
        this.etiqueta = etiqueta;
        this.tamano = tamano;
        this.valor = valor;
    }

    // Getters...
}