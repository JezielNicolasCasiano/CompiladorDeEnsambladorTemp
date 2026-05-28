package jeziel.compiladordeensamblador.modelo;

import jeziel.compiladordeensamblador.modelo.parser.ResultadoParser;
import jeziel.compiladordeensamblador.modelo.semantico.ResultadoSemantico;

import java.awt.*;

public class FilaMaquina {
    private final String contador; //Contador logico empieza desde 470h en cada segmento
    private final String linea;
    private final String codigoMaquina;
    private final String resultado;

    public FilaMaquina(String contador, String linea, String codigoMaquina, String resultado) {
        this.contador = contador;
        this.linea = linea;
        this.codigoMaquina = codigoMaquina;
        this.resultado = resultado;
    }

    public FilaMaquina crearDesdeParser(ResultadoParser resultadoParser, ResultadoSemantico resultadoSemantico, TextArea la){

        return null;
    }
}
