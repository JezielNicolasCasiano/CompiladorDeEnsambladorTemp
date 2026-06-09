package jeziel.compiladordeensamblador.modelo.semantico;

import jeziel.compiladordeensamblador.modelo.parser.LineaAnalizada;

import java.util.ArrayList;
import java.util.List;

public class AnalizadorSemantico {
    private List<LineaAnalizada> arbolSintactico;
    private AnalizadorNodoSintactico analizadorNodoSintactico;
    private List<LineaAnalizadaSemanticamente> tablaDeSimbolos;
    private List<LineaAnalizadaSemanticamente> analisisSemantico;

    public AnalizadorSemantico(List<LineaAnalizada> arbolSintactico) {
        this.arbolSintactico = arbolSintactico;
    }

    // Metodo para ir recorriendo el arbolSintactico e ir analizando semanticamente, primero para encontrar y rellenar la tabla de simbolos y despues
    // para todos los demas tokens
    public List<LineaAnalizadaSemanticamente> analizarBuscandoSimbolos(){
        this.tablaDeSimbolos = new ArrayList<>();
        this.analizadorNodoSintactico = new AnalizadorNodoSintactico(arbolSintactico);

        while (analizadorNodoSintactico.getLineaSintacticaActual() < arbolSintactico.size()) {
            LineaAnalizadaSemanticamente sym = analizadorNodoSintactico.buscarSimbolos(tablaDeSimbolos);
            if (sym != null) {
                tablaDeSimbolos.add(sym);
            }
        }
        return tablaDeSimbolos;
    }

    public List<LineaAnalizadaSemanticamente> analizar() {
        // Asegurar que la tabla de símbolos está construida
        if (this.tablaDeSimbolos == null) {
            analizarBuscandoSimbolos();
        }

        this.analisisSemantico = new ArrayList<>();
        this.analizadorNodoSintactico.reset();

        while (analizadorNodoSintactico.getLineaSintacticaActual() < arbolSintactico.size()) {
            LineaAnalizadaSemanticamente lineaSem = analizadorNodoSintactico.analizar(tablaDeSimbolos);
            if (lineaSem != null) {
                analisisSemantico.add(lineaSem);
            }
        }

        return analisisSemantico;
    }

    public List<LineaAnalizadaSemanticamente> getTablaDeSimbolos() {
        return tablaDeSimbolos;
    }

    public List<LineaAnalizadaSemanticamente> getAnalisisSemantico() {
        return analisisSemantico;
    }
}
