package jeziel.compiladordeensamblador.modelo;

import jeziel.compiladordeensamblador.modelo.parser.ResultadoParser;
import jeziel.compiladordeensamblador.modelo.semantico.ResultadoSemantico;

import java.awt.*;

public class FilaMaquina {
    private final int contador; //Contador logico empieza desde 470h en cada segmento
    private final String linea;
    private final String codigoMaquina;
    private final String resultado;

    public FilaMaquina(int contador, String linea, String codigoMaquina, String resultado) {
        this.contador = contador;
        this.linea = linea;
        this.codigoMaquina = codigoMaquina;
        this.resultado = resultado;
    }

    public FilaMaquina crearDesdeParser(String resultado, int contador, String linea, String codigoMaquina){

        return null;
    }

    public int getContador() {
        return contador;
    }

    public String getLinea() {
        return linea;
    }

    public String getCodigoMaquina() {
        return codigoMaquina;
    }

    public String getResultado() {
        return resultado;
    }
}
