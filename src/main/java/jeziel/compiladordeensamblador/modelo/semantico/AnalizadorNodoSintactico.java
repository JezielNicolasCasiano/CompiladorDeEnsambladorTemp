package jeziel.compiladordeensamblador.modelo.semantico;

import jeziel.compiladordeensamblador.modelo.lexer.TokenSubtype;
import jeziel.compiladordeensamblador.modelo.lexer.TokenType;
import jeziel.compiladordeensamblador.modelo.parser.AnalizadorGeneral;
import jeziel.compiladordeensamblador.modelo.parser.LineaAnalizada;

import java.util.List;

public class AnalizadorNodoSintactico {
    private List<LineaAnalizada> arbolSintactico;
    private int lineaSintacticaActual;
    private LineaAnalizadaSemanticamente linea;
    private int tokenSintacticoActual;

    public AnalizadorNodoSintactico(List<LineaAnalizada> arbolSintactico){
        this.arbolSintactico = arbolSintactico;
        this.lineaSintacticaActual = 0;
    }

    //Metodo para recorrer el arbol sinctactico e ir delegando

    public LineaAnalizadaSemanticamente analizar(){
        linea = new LineaAnalizadaSemanticamente(arbolSintactico.get(lineaSintacticaActual));
        if(arbolSintactico.get(lineaSintacticaActual).tieneError()){
            lineaSintacticaActual++;
            return linea;
        }
        switch ()



    }

    public LineaAnalizadaSemanticamente buscarSimbolos(){ //Si la linea entonctro un simbolo lo regresa, si no manda null que se desecha en el anlizador semantico
        linea = new LineaAnalizadaSemanticamente(arbolSintactico.get(lineaSintacticaActual));
        if(arbolSintactico.get(lineaSintacticaActual).tieneError() || !(arbolSintactico.get(lineaSintacticaActual).getTokens().getFirst().getType().equals(TokenType.VARIABLE)) || !(arbolSintactico.get(lineaSintacticaActual).getTokens().getFirst().getType().equals(TokenType.ETIQUETA) || !(arbolSintactico.get(lineaSintacticaActual).getTokens().getFirst().getSub().equals(TokenSubtype.Directiva.))){
            lineaSintacticaActual++;
            return null;
        }
        switch (arbolSintactico.get(lineaSintacticaActual).getTokens().getFirst().getType()){
            case VARIABLE ->
            case ETIQUETA ->
            case PSEUDOINSTRUCCION ->
            case
            default ->
                    throw new IllegalStateException("Unexpected value: " + arbolSintactico.get(lineaSintacticaActual).getTokens().getFirst());
        }



    }



}
