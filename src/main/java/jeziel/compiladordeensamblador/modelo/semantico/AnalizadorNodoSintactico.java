package jeziel.compiladordeensamblador.modelo.semantico;

import jeziel.compiladordeensamblador.modelo.lexer.Token;
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

        return null;

    }

    public LineaAnalizadaSemanticamente buscarSimbolos() {
        if (lineaSintacticaActual >= arbolSintactico.size()) {
            return null;
        }

        LineaAnalizada lineaActual = arbolSintactico.get(lineaSintacticaActual);

        if (lineaActual.tieneError() || lineaActual.getTokens().isEmpty()) {
            lineaSintacticaActual++;
            return null;
        }

        Token primerToken = lineaActual.getTokens().getFirst();

        switch (primerToken.getType()) {
            case VARIABLE:
                linea = new LineaAnalizadaSemanticamente(lineaActual);

                // Aquí delegarás el análisis a tu clase semántica específica
                // AnalizarSemanticaVariable analizadorVar = new AnalizarSemanticaVariable(primerToken, lineaActual);
                // analizadorVar.analizar();

                lineaSintacticaActual++;
                return linea;

            case ETIQUETA:
                linea = new LineaAnalizadaSemanticamente(lineaActual);

                // Aquí delegarás a la clase específica de etiquetas
                // AnalizarSemanticaEtiqueta analizadorEtq = new AnalizarSemanticaEtiqueta(primerToken, lineaActual);
                // analizadorEtq.analizar();

                lineaSintacticaActual++;
                return linea;

            default:

                lineaSintacticaActual++;
                return null;
        }
    }

    public int getLineaSintacticaActual() {
        return lineaSintacticaActual;
    }



    }




