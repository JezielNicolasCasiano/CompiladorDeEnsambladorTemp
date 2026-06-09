package jeziel.compiladordeensamblador.modelo.semantico;

import jeziel.compiladordeensamblador.modelo.parser.LineaAnalizada;

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




        return null;
    }

    public List<LineaAnalizadaSemanticamente> analizar() {





        return null;
    }
}
