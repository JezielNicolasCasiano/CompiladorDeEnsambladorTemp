package jeziel.compiladordeensamblador.modelo.semantico;

import jeziel.compiladordeensamblador.modelo.lexer.Token;
import java.util.List;

public class DepuradorSemantico {

    public static void depurar(List<LineaAnalizadaSemanticamente> lineas) {
        if (lineas == null) return;
        for (LineaAnalizadaSemanticamente linea : lineas) {
            // Primero error sintáctico
            if (linea.getLineaAnalizada() != null && linea.getLineaAnalizada().tieneError()) {
                Token tokenErroneo = linea.getLineaAnalizada().getErrorSintactico().getTokenErroneo();
                String mensaje = linea.getLineaAnalizada().getErrorSintactico().getMensajeError();
                System.out.println("Error Sintáctico - Token: " + (tokenErroneo != null ? tokenErroneo.getValue() : "null") 
                                   + " en línea " + linea.getLineaAnalizada().getNumeroLinea()
                                   + " | Mensaje: " + mensaje);
            }
            // Después error semántico
            else if (linea.getErrorSemantico() != null) {
                Token tokenErroneo = linea.getErrorSemantico().getTokenErroneo();
                String mensaje = linea.getErrorSemantico().getMensajeError();
                System.out.println("Error Semántico - Token: " + (tokenErroneo != null ? tokenErroneo.getValue() : "null") 
                                   + " en línea " + (tokenErroneo != null ? tokenErroneo.getLinea() : "desconocida")
                                   + " | Mensaje: " + mensaje);
            }
        }
    }
}
