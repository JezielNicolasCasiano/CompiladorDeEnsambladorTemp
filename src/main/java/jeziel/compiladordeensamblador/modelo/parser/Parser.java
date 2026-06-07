package jeziel.compiladordeensamblador.modelo.parser;

import jeziel.compiladordeensamblador.modelo.lexer.Token;

import java.util.List;

public class Parser {
    List<List<Token>> ArbolLineal;
    List<Token> tokens;
    int actual;
    AnalizadorLineaSintactica analizadorLineaSintactica;

    public Parser(List<Token> tokens){
        this.tokens = tokens;
        this.actual = 0;
        this.analizadorLineaSintactica = new AnalizadorLineaSintactica(this.tokens);

    }

    public Token avanzarToken(){
        return tokens.get(actual+1);
    }

    public List<List<Token>> parsear(){
        /* Metodo princial, orquesta toda la operacion pero no hace directamente el analisis, para eso esta el anlizadorLineaSintactico que lo hace
        linea por linea. En esta parte tiene que haber un ciclo que vaya cambiando la liena del analizador linea por linea, ademas de ir armando el arbol
        final.
         */

        return null;
    }

}
